package de.syntaxjason.subathon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "timers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private Integer remainingSeconds;

    @Column(nullable = false)
    private Integer initialSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimerStatus status;

    @Column
    private LocalDateTime lastTickAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TimerStatus.STOPPED;
        }
    }

    public enum TimerStatus {
        RUNNING,
        PAUSED,
        STOPPED
    }
}
