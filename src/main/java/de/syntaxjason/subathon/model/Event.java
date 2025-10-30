package de.syntaxjason.subathon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer secondsAdded;

    @Column
    private String username;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public enum EventType {
        SUBSCRIPTION,
        BITS,
        REWARD,
        MANUAL_ADD,
        TIMER_START,
        TIMER_PAUSE,
        TIMER_RESET,
        TIMER_END
    }
}
