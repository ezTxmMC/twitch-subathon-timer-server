package de.syntaxjason.subathon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_toggles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventToggle {

    @Id
    private String sessionId;

    @Column(nullable = false)
    private Boolean followEnabled = true;

    @Column(nullable = false)
    private Boolean subscriptionEnabled = true;

    @Column(nullable = false)
    private Boolean giftedSubEnabled = true;

    @Column(nullable = false)
    private Boolean bitsEnabled = true;

    @Column(nullable = false)
    private Boolean raidEnabled = true;

    @Column(nullable = false)
    private Boolean hostEnabled = false; // Host is deprecated on Twitch
}
