package ma.smartflow.authserver.mappers;

import ma.smartflow.authserver.dtos.AuthResponse;
import ma.smartflow.authserver.dtos.RegisterRequest;
import ma.smartflow.authserver.dtos.UserMcpResponse;
import ma.smartflow.authserver.entities.User;
import ma.smartflow.authserver.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(RegisterRequest request){
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .role(Role.USER)
                .build();
    }

    public AuthResponse fromUser(User user){
        return AuthResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public UserMcpResponse toUserMcpResponse(User user){
        return UserMcpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
