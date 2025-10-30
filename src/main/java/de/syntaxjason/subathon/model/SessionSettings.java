package de.syntaxjason.subathon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionSettings {

    @Id
    private String sessionId;

    @Column(nullable = false)
    private Integer followSeconds = 30;

    @Column(nullable = false)
    private Integer subTier1Seconds = 60;

    @Column(nullable = false)
    private Integer subTier2Seconds = 120;

    @Column(nullable = false)
    private Integer subTier3Seconds = 300;

    @Column(nullable = false)
    private Integer giftedSubSeconds = 60;

    @Column(nullable = false)
    private Integer bitsPer100 = 100;

    @Column(nullable = false)
    private Integer raidSecondsPerViewer = 1;

    @Column(nullable = false)
    private String overlayTheme = "dark";

    @Column(nullable = false)
    private String overlayFontSize = "medium";

    @Column(nullable = false)
    private String overlayPosition = "top-right";

    @Column(nullable = false)
    private Boolean overlayAnimations = true;
}
