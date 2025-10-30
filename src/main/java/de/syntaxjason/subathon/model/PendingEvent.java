package de.syntaxjason.subathon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pending_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String channelId;

    @Column(nullable = false)
    private String channelName;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Integer amount;

    @Column
    private String username;

    @Column(nullable = false)
    private Boolean processed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime processedAt;
}
