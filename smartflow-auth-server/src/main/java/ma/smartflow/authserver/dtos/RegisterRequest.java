package ma.smartflow.authserver.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "username obligatoire")
    private String username;

    @NotBlank(message = "email oblogatoire")
    private String email;

    @NotBlank(message = "password obligatoire")
    @Size(min = 6)
    private String password;
}
