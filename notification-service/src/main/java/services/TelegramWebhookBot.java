package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.TelegramLink;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import repositories.TelegramLinkRepository;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class TelegramWebhookBot {

    private final TelegramBotService telegramBotService;
    private final TelegramLinkRepository telegramLinkRepository;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramWebhookBot(TelegramBotService telegramBotService,
                              TelegramLinkRepository telegramLinkRepository) {
        this.telegramBotService = telegramBotService;
        this.telegramLinkRepository = telegramLinkRepository;
        this.restTemplate = new RestTemplate();
    }

    // Endpoint qui reçoit les updates de Telegram
    // Appelé via webhook ou polling
    public void processUpdate(Map<String, Object> update) {
        try {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) return;

            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            Long chatId = ((Number) chat.get("id")).longValue();
            String text = (String) message.get("text");

            if (text == null) return;

            log.info("📩 Telegram message from chatId={}: {}", chatId, text);

            if (text.startsWith("/start")) {
                handleStart(chatId);
            } else if (text.startsWith("/link ")) {
                handleLink(chatId, text);
            } else {
                handleAiChat(chatId, text);
            }
        } catch (Exception e) {
            log.error("❌ Error processing Telegram update: {}", e.getMessage());
        }
    }

    private void handleStart(Long chatId) {
        String welcome = """
                🧠 *Bienvenue sur SmartFlow AI !*
                
                Je suis votre assistant intelligent de gestion de tâches.
                
                *Commandes :*
                /link username password — Lier votre compte
                
                *Ensuite, parlez-moi naturellement :*
                • "Montre mes tâches"
                • "Crée une tâche urgente : ..."
                • "Combien de tâches j'ai en cours ?"
                • "Marque la tâche 5 comme terminée"
                """;
        telegramBotService.sendMessage(chatId, welcome);
    }

    private void handleLink(Long chatId, String text) {
        // /link username password
        String[] parts = text.split(" ");
        if (parts.length != 3) {
            telegramBotService.sendMessage(chatId, "❌ Usage : /link username password");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        try {
            // Authentifier via le Gateway
            Map<String, String> loginBody = Map.of(
                    "username", username,
                    "password", password
            );

            ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                    "http://localhost:8080/api/auth/login",
                    loginBody,
                    Map.class
            );

            if (loginResponse.getStatusCode().is2xxSuccessful()) {
                String token = (String) loginResponse.getBody().get("token");

                // Lier le chatId au userId via le Gateway
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Long> linkBody = Map.of("chatId", chatId);
                HttpEntity<Map<String, Long>> entity = new HttpEntity<>(linkBody, headers);

                restTemplate.postForEntity(
                        "http://localhost:8080/api/notifications/link",
                        entity,
                        Map.class
                );

                telegramBotService.sendMessage(chatId,
                        "✅ Compte lié avec succès ! Vous pouvez maintenant me parler.");
            }
        } catch (Exception e) {
            log.error("❌ Link error: {}", e.getMessage());
            telegramBotService.sendMessage(chatId,
                    "❌ Échec de l'authentification. Vérifiez vos identifiants.");
        }
    }

    private void handleAiChat(Long chatId, String text) {
        // Trouver l'utilisateur lié
        Optional<TelegramLink> linkOpt = telegramLinkRepository.findByChatId(chatId);

        if (linkOpt.isEmpty()) {
            telegramBotService.sendMessage(chatId,
                    "⚠️ Vous devez d'abord lier votre compte : /link username password");
            return;
        }

        TelegramLink link = linkOpt.get();

        try {
            // Authentifier pour avoir un token
            // En prod, on stockerait le token — ici on simplifie
            // Appeler le AI Service via Gateway
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", link.getUserId().toString());
            headers.set("X-User-Username", link.getUsername());

            Map<String, String> body = Map.of("message", text);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            // Appel direct au AI Service (service interne, pas via Gateway)
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8083/api/ai/chat",
                    entity,
                    Map.class
            );

            String aiResponse = (String) response.getBody().get("response");
            telegramBotService.sendMessage(chatId, aiResponse);

        } catch (Exception e) {
            log.error("❌ AI chat error: {}", e.getMessage());
            telegramBotService.sendMessage(chatId,
                    "❌ Désolé, une erreur est survenue. Réessayez.");
        }
    }
}