package de.syntaxjason.subathon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String channelName;

    @Column(nullable = false)
    private String channelId;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column
    private String eventSubConnectionId;
}
