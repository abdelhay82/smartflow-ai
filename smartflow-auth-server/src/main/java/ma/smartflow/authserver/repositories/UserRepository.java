package ma.smartflow.authserver.repositories;

import ma.smartflow.authserver.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existByUsername(String username);
    boolean existByEmail(String email);
}
