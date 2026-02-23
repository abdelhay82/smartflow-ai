package ma.smartflow.aiservice.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ChatRequest {

    @NotBlank
    private String message;
}
