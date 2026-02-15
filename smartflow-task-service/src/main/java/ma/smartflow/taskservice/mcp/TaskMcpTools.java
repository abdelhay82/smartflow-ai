package ma.smartflow.taskservice.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.smartflow.taskservice.dtos.TaskEvent;
import ma.smartflow.taskservice.dtos.TaskRequest;
import ma.smartflow.taskservice.dtos.TaskResponse;
import ma.smartflow.taskservice.entities.Task;
import ma.smartflow.taskservice.enums.Priority;
import ma.smartflow.taskservice.enums.Status;
import ma.smartflow.taskservice.kafkaProducer.TaskEventProducer;
import ma.smartflow.taskservice.mappers.TaskMapper;
import ma.smartflow.taskservice.repositories.TaskRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskMcpTools {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;

    @McpTool(name = "list_tasks", description =
            "List all tasks for a given user. Returns task id, title, description, status, priority and dates.")
    public List<TaskMcpResponse> listTasks(
            @McpToolParam(description = "The user ID to list tasks for") Long userId) {

        log.info("🔧 MCP Tool called: list_tasks for userId={}", userId);

        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toMcpResponse)
                .toList();
    }

    @McpTool(name = "create_task", description =
            "Create a new task for a user. Requires a title, and optionally a description and priority (LOW, MEDIUM, HIGH, URGENT). Returns the created task.")
    public TaskMcpResponse createTask(
            @McpToolParam(description = "The user ID who owns the task") Long userId,
            @McpToolParam(description = "The username of the task owner") String username,
            @McpToolParam(description = "Title of the task") String title,
            @McpToolParam(description = "Description of the task", required = false) String description,
            @McpToolParam(description = "Priority: LOW, MEDIUM, HIGH, or URGENT", required = false) String priority) {

        log.info("🔧 MCP Tool called: create_task '{}' for user={}", title, username);

        Task task = Task.builder()
                .title(title)
                .description(description)
                .priority(priority != null ? Priority.valueOf(priority.toUpperCase()) : Priority.MEDIUM)
                .status(Status.TODO)
                .userId(userId)
                .username(username)
                .build();

        task = taskRepository.save(task);

        // Kafka event
        taskEventProducer.sendTaskEvent(buildEvent(task, "CREATED"));

        return toMcpResponse(task);
    }

    @McpTool(name = "complete_task", description =
            "Mark a task as completed. Returns the updated task.")
    public TaskMcpResponse completeTask(
            @McpToolParam(description = "The task ID to complete") Long taskId,
            @McpToolParam(description = "The user ID (for authorization check)") Long userId) {

        log.info("🔧 MCP Tool called: complete_task taskId={}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: this task belongs to another user");
        }

        task.setStatus(Status.DONE);
        task = taskRepository.save(task);

        taskEventProducer.sendTaskEvent(buildEvent(task, "COMPLETED"));

        return toMcpResponse(task);
    }

    @McpTool(name = "delete_task", description =
            "Delete a task by its ID. Returns a confirmation message.")
    public String deleteTask(
            @McpToolParam(description = "The task ID to delete") Long taskId,
            @McpToolParam(description = "The user ID (for authorization check)") Long userId) {

        log.info("🔧 MCP Tool called: delete_task taskId={}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: this task belongs to another user");
        }

        taskRepository.delete(task);
        taskEventProducer.sendTaskEvent(buildEvent(task, "DELETED"));

        return "Task '" + task.getTitle() + "' (id=" + taskId + ") has been deleted successfully.";
    }

    @McpTool(name = "search_tasks_by_status", description =
            "Search tasks by status for a given user. Status can be: TODO, IN_PROGRESS, DONE, CANCELLED.")
    public List<TaskMcpResponse> searchTasksByStatus(
            @McpToolParam(description = "The user ID") Long userId,
            @McpToolParam(description = "Task status: TODO, IN_PROGRESS, DONE, or CANCELLED") String status) {

        log.info("🔧 MCP Tool called: search_tasks_by_status status={} for userId={}", status, userId);

        return taskRepository.findByUserIdAndStatus(userId, Status.valueOf(status.toUpperCase()))
                .stream()
                .map(this::toMcpResponse)
                .toList();
    }

    @McpTool(name = "get_task_summary", description =
            "Get a summary of task counts by status for a user. Useful for dashboards and reports.")
    public TaskSummary getTaskSummary(
            @McpToolParam(description = "The user ID") Long userId) {

        log.info("🔧 MCP Tool called: get_task_summary for userId={}", userId);

        List<Task> allTasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId);

        long todo = allTasks.stream().filter(t -> t.getStatus() == Status.TODO).count();
        long inProgress = allTasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
        long done = allTasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        long cancelled = allTasks.stream().filter(t -> t.getStatus() == Status.CANCELED).count();

        return new TaskSummary(allTasks.size(), todo, inProgress, done, cancelled);
    }

    // ── DTOs spécifiques MCP ──────────────────────

    public record TaskMcpResponse(
            Long id,
            String title,
            String description,
            String status,
            String priority,
            String username,
            String createdAt
    ) {}

    public record TaskSummary(
            long total,
            long todo,
            long inProgress,
            long done,
            long cancelled
    ) {}

    // ── Mappers ───────────────────────────────────

    private TaskMcpResponse toMcpResponse(Task task) {
        return new TaskMcpResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getUsername(),
                task.getCreatedAt() != null ? task.getCreatedAt().toString() : null
        );
    }

    private TaskEvent buildEvent(Task task, String eventType) {
        return TaskEvent.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .userId(task.getUserId())
                .username(task.getUsername())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .build();
    }
}