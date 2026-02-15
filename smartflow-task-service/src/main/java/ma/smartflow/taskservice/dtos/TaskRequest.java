package ma.smartflow.taskservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ma.smartflow.taskservice.enums.Priority;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "title not null")
    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime dueDate;
}
