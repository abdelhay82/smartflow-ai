package ma.smartflow.aiservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider toolCallbackProvider;

    private static final String SYSTEM_PROMPT = """
            Tu es un smartflow AI, un assistant intelligent pour la gestion des taches.
            Tu as acces à des outils pour:
            - Gérer les taches (créer, lister, supprimer, chercher)
            - Consulter les informations des utilistaeurs
            
            Quand un utilisateur te demande quelque chose en rapport avec ses taches ou son compte, utilise les outils disponilbes.
            
            Repond toujours en français, de maniere concise et professionnelle.
            Utilise des emojis pour rendre la reponse plus lisibles.
            
            Context utilisateur:
            - User Id: {userId}
            - Username: {username}
            """;

    public String chat(String message, Long userId, String username){
        log.info("Chat request from user={} : {}", username, message);

        ChatClient chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT
                        .replace("{userId}", userId.toString())
                        .replace("{username}", username))
                .defaultToolCallbacks(toolCallbackProvider)
                .build();

        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    public Flux<String> chatStream(String message, Long userId, String username){
        log.info("Stream Chat request from user={} : {}", username, message);

        ChatClient chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT
                        .replace("{userId}", userId.toString())
                        .replace("{username}", username))
                .defaultToolCallbacks(toolCallbackProvider)
                .build();

        return chatClient.prompt()
                .user(message)
                .stream()
                .content();


    }
}
