package org.example.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "rooms", indexes = {
        @Index(columnList = "name"),
        @Index(columnList = "is_private")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"users", "messages"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isPrivate;

    @Builder.Default
    @ManyToMany(mappedBy = "rooms", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "room",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Message> messages = new ArrayList<>();

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void addUser(User user) {
        if (users.add(user)) {
            user.getRooms().add(this);
        }
    }

    public void removeUser(User user) {
        if (users.remove(user)) {
            user.getRooms().remove(this);
        }
    }

    public boolean containsUser(User user) {
        return users.contains(user);
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setRoom(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        return id != null && id.equals(((Room) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}