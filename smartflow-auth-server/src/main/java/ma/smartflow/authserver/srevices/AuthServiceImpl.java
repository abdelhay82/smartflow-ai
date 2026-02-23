package ma.smartflow.authserver.srevices;

import lombok.RequiredArgsConstructor;
import ma.smartflow.authserver.dtos.AuthResponse;
import ma.smartflow.authserver.dtos.LoginRequest;
import ma.smartflow.authserver.dtos.RegisterRequest;
import ma.smartflow.authserver.entities.User;
import ma.smartflow.authserver.mappers.UserMapper;
import ma.smartflow.authserver.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existByEmail(request.getEmail())) {
            throw new RuntimeException("Emaim already exists");
        }

        if(userRepository.existByUsername(request.getUsername())){
            throw new RuntimeException("Username already exist");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        String jwt = jwtService.generateToken(saved);

        AuthResponse response = userMapper.fromUser(saved);
        response.setToken(jwt);
        return response;

    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        AuthResponse response = userMapper.fromUser(user);
        response.setToken(token);
        return response;
    }
}
