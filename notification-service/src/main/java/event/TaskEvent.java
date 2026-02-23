package event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEvent {
    private Long taskId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long userId;
    private String username;
    private String eventType;
    private LocalDateTime timestamp;
}
