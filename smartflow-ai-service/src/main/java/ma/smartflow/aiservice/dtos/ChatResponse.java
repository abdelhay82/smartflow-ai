package ma.smartflow.aiservice.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {

    private String response;
    private String username;
    private LocalDateTime timeStamp;
}
