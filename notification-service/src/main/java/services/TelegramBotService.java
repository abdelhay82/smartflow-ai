package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TelegramBotService {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${telegram.bot.token}")
    private String botToken;

    public void sendMessage(Long chatId, String text) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        try {
            Map<String, Object> body = Map.of(
                    "chat_id", chatId,
                    "text", text,
                    "parse_mode", "Markdown"
            );

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("✅ Telegram message sent to chatId={}", chatId);
                } else {
                    log.error("❌ Telegram API error: {}", response.body().string());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram message: {}", e.getMessage());
        }
    }
}