package ma.smartflow.authserver.srevices;


import ma.smartflow.authserver.dtos.AuthResponse;
import ma.smartflow.authserver.dtos.LoginRequest;
import ma.smartflow.authserver.dtos.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
