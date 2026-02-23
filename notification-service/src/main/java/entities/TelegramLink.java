package entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    @Column(name = "linked_at")
    private LocalDateTime linkedAt;

    @PrePersist
    protected void onCreate(){
        linkedAt = LocalDateTime.now();
    }
}
