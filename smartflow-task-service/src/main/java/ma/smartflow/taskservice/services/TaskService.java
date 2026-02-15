package ma.smartflow.taskservice.services;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;
    private final TaskMapper taskMapper;

    public TaskResponse createTask(TaskRequest request, Long userId, String username){
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getTitle())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(Status.TODO)
                .userId(userId)
                .username(username)
                .dueDate(request.getDueDate())
                .build();
        task = taskRepository.save(task);
        taskEventProducer.sendTaskEvent(taskMapper.buildEvent(task, "CREATED"));
        return taskMapper.mapToResponse(task);
    }

    @McpTool(name = "lists_tasks", description = "List all tasks for a given user. Returns task id, title, description, status, priority and date.")
    public List<TaskResponse> getMyTasks(@McpToolParam(description = "the user Id to list tasks for") Long userId){
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(taskMapper::mapToResponse)
                .toList();
    }

    public TaskResponse getTask(Long taskId, Long userId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)){
            throw new RuntimeException("Access denied");
        }

        return taskMapper.mapToResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        task = taskRepository.save(task);

        taskEventProducer.sendTaskEvent(taskMapper.buildEvent(task, "UPDATED"));

        return taskMapper.mapToResponse(task);
    }

    public TaskResponse completeTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        task.setStatus(Status.DONE);
        task = taskRepository.save(task);

        taskEventProducer.sendTaskEvent(taskMapper.buildEvent(task, "COMPLETED"));

        return taskMapper.mapToResponse(task);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        taskRepository.delete(task);

        taskEventProducer.sendTaskEvent(taskMapper.buildEvent(task, "DELETED"));
    }
}
