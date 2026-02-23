package ma.smartflow.authserver.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMcpResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
}
