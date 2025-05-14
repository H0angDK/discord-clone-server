package org.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tokens", indexes = {
        @Index(name = "token_idx", columnList = "token"),
        @Index(name = "user_id_idx", columnList = "user_id")
})
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_revoked")
    private boolean isRevoked;

    @Column(name = "token_type")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    public enum TokenType {
        ACCESS,
        REFRESH
    }
}
