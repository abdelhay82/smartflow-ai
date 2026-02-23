package controller;

import entities.TelegramLink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repositories.TelegramLinkRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class LinkController {

    private final TelegramLinkRepository telegramLinkRepository;

    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> linkTelegram(
            @RequestBody Map<String, Long> body,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Username") String username) {

        Long chatId = body.get("chatId");

        if (telegramLinkRepository.existsByUserId(userId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Account already linked"));
        }

        TelegramLink link = TelegramLink.builder()
                .userId(userId)
                .username(username)
                .chatId(chatId)
                .build();

        telegramLinkRepository.save(link);
        log.info("🔗 Telegram linked: user={} → chatId={}", username, chatId);

        return ResponseEntity.ok(Map.of("message", "Telegram linked successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(
            @RequestHeader("X-User-Id") Long userId) {

        boolean linked = telegramLinkRepository.existsByUserId(userId);
        return ResponseEntity.ok(Map.of("linked", linked));
    }
}
