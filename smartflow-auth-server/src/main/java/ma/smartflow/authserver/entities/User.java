package ma.smartflow.authserver.entities;


import jakarta.persistence.*;
import lombok.*;
import ma.smartflow.authserver.enums.Role;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username_user", columnList = "username"),
        @Index(name = "idx_email_user", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
