package ma.smartflow.aiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.smartflow.aiservice.dtos.ChatRequest;
import ma.smartflow.aiservice.dtos.ChatResponse;
import ma.smartflow.aiservice.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("X_User_Id") Long userId,
            @RequestHeader("X_User_Username") String username
            ){

        String response = aiService.chat(request.getMessage(), userId, username);

        return ResponseEntity.ok(ChatResponse.builder()
                        .response(response)
                        .username(username)
                        .timeStamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/chat/stream")
    public Flux<ServerSentEvent<String>> chatStream(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("X_User_Id") Long userId,
            @RequestHeader("X_User_Username") String username
    ){

        String response = aiService.chat(request.getMessage(), userId, username);

        return aiService.chatStream(request.getMessage(),  userId, username)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());

    }
}
