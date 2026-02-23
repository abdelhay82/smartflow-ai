package ma.smartflow.authserver.srevices;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.smartflow.authserver.dtos.UserMcpResponse;
import ma.smartflow.authserver.entities.User;
import ma.smartflow.authserver.mappers.UserMapper;
import ma.smartflow.authserver.repositories.UserRepository;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMcpTools {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @McpTool(name = "get_user_info", description = " Get user information by user id. return username, email and role.")
    public UserMcpResponse getUserInfo(@McpToolParam(description = "The user id") Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found exception"));

        log.info("Mcp tool called: get_user_info for userId={}", userId);

        return userMapper.toUserMcpResponse(user);
    }

    @McpTool(name = "get_user_by_username", description = "Get user information by username. return userId, email and role")
    public UserMcpResponse getUserByUsername(String username){
        log.info("Mcp tool called: get_user_by_username for username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toUserMcpResponse(user);
    }

    @McpTool(name = "list_all_users", description = "List all users. Returns a list of usernames, emails and roles. Useful for admin queries")
    public List<UserMcpResponse> listAllUsers(){
        log.info("Mcp tool called: list_all_users");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserMcpResponse)
                .toList();
    }

    @McpTool(name = "check_user_exists", description = "cheek if user exists by username or email. returns true or false")
    public String checkUserExists(@McpToolParam(description = "username or email to check") String identifier){

        log.info("Mcp tool called: check_user_exists for identifier={}", identifier);

        boolean exists = userRepository.existByUsername(identifier) || userRepository.existByEmail(identifier);

        return exists ?
                "User " + identifier + " exists in the system"
                : "User " + identifier + " does not exists in the system";
    }

}
