package repositories;

import entities.TelegramLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramLinkRepository extends JpaRepository<TelegramLink, Long> {

    Optional<TelegramLink> findByUserId(Long userId);
    Optional<TelegramLink> findByChatId(Long chatId);
    boolean existsByUserId(Long userId);
}

