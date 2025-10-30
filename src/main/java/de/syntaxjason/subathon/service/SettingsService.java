package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.SessionSettings;
import de.syntaxjason.subathon.repository.SessionSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final SessionSettingsRepository settingsRepository;

    public SessionSettings getSettings(String sessionId) {
        return settingsRepository.findById(sessionId)
                .orElseGet(() -> createDefaultSettings(sessionId));
    }

    @Transactional
    public SessionSettings createDefaultSettings(String sessionId) {
        SessionSettings settings = SessionSettings.builder()
                .sessionId(sessionId)
                .followSeconds(30)
                .subTier1Seconds(60)
                .subTier2Seconds(120)
                .subTier3Seconds(300)
                .giftedSubSeconds(60)
                .bitsPer100(100)
                .raidSecondsPerViewer(1)
                .overlayTheme("dark")
                .overlayFontSize("medium")
                .overlayPosition("top-right")
                .overlayAnimations(true)
                .build();

        return settingsRepository.save(settings);
    }

    @Transactional
    public SessionSettings updateSettings(String sessionId, SessionSettings newSettings) {
        SessionSettings settings = getSettings(sessionId);

        settings.setFollowSeconds(newSettings.getFollowSeconds());
        settings.setSubTier1Seconds(newSettings.getSubTier1Seconds());
        settings.setSubTier2Seconds(newSettings.getSubTier2Seconds());
        settings.setSubTier3Seconds(newSettings.getSubTier3Seconds());
        settings.setGiftedSubSeconds(newSettings.getGiftedSubSeconds());
        settings.setBitsPer100(newSettings.getBitsPer100());
        settings.setRaidSecondsPerViewer(newSettings.getRaidSecondsPerViewer());
        settings.setOverlayTheme(newSettings.getOverlayTheme());
        settings.setOverlayFontSize(newSettings.getOverlayFontSize());
        settings.setOverlayPosition(newSettings.getOverlayPosition());
        settings.setOverlayAnimations(newSettings.getOverlayAnimations());

        return settingsRepository.save(settings);
    }
}
