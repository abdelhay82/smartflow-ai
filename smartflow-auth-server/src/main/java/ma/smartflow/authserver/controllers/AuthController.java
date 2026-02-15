package ma.smartflow.authserver.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.smartflow.authserver.dtos.AuthResponse;
import ma.smartflow.authserver.dtos.LoginRequest;
import ma.smartflow.authserver.dtos.RegisterRequest;
import ma.smartflow.authserver.srevices.AuthService;
import ma.smartflow.authserver.srevices.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestParam String token){
        return ResponseEntity.ok(jwtService.isTokenValid(token));
    }
}
