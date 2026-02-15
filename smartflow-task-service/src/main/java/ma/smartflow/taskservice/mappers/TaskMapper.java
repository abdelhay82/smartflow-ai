package ma.smartflow.taskservice.mappers;

import ma.smartflow.taskservice.dtos.TaskEvent;
import ma.smartflow.taskservice.dtos.TaskResponse;
import ma.smartflow.taskservice.entities.Task;

import java.time.LocalDateTime;

public class TaskMapper {

    public TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .username(task.getUsername())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public TaskEvent buildEvent(Task task, String eventType){
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
