package event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import repositories.TelegramLinkRepository;
import services.TelegramBotService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final TelegramLinkRepository telegramLinkRepository;
    private final TelegramBotService telegramBotService;

    @KafkaListener(topics = "task-events", groupId = "notification-group")
    public void handleTaskEvent(TaskEvent event) {
        log.info("📥 Kafka event received: {} for task '{}'", event.getEventType(), event.getTitle());

        // Chercher si l'utilisateur a lié son Telegram
        telegramLinkRepository.findByUserId(event.getUserId())
                .ifPresent(link -> {
                    String message = formatMessage(event);
                    telegramBotService.sendMessage(link.getChatId(), message);
                });
    }

    private String formatMessage(TaskEvent event) {
        String emoji = switch (event.getEventType()) {
            case "CREATED" -> "📋";
            case "COMPLETED" -> "✅";
            case "UPDATED" -> "✏️";
            case "DELETED" -> "🗑️";
            default -> "📌";
        };

        String priorityEmoji = switch (event.getPriority()) {
            case "URGENT" -> "🔴";
            case "HIGH" -> "🟠";
            case "MEDIUM" -> "🟡";
            case "LOW" -> "🟢";
            default -> "⚪";
        };

        return String.format("""
                %s *Tâche %s*
                
                📝 *%s*
                %s Priorité : %s
                📊 Statut : %s
                👤 Par : %s
                🕐 %s""",
                emoji,
                event.getEventType().toLowerCase(),
                event.getTitle(),
                priorityEmoji,
                event.getPriority(),
                event.getStatus(),
                event.getUsername(),
                event.getTimestamp().toString()
        );
    }
}
