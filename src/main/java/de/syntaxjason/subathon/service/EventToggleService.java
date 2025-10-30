package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.EventToggle;
import de.syntaxjason.subathon.repository.EventToggleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventToggleService {

    private final EventToggleRepository toggleRepository;

    public EventToggle getToggles(String sessionId) {
        return toggleRepository.findById(sessionId)
                .orElseGet(() -> createDefaultToggles(sessionId));
    }

    @Transactional
    public EventToggle createDefaultToggles(String sessionId) {
        EventToggle toggles = EventToggle.builder()
                .sessionId(sessionId)
                .followEnabled(true)
                .subscriptionEnabled(true)
                .giftedSubEnabled(true)
                .bitsEnabled(true)
                .raidEnabled(true)
                .hostEnabled(false)
                .build();

        return toggleRepository.save(toggles);
    }

    @Transactional
    public EventToggle updateToggles(String sessionId, EventToggle newToggles) {
        EventToggle toggles = getToggles(sessionId);

        toggles.setFollowEnabled(newToggles.getFollowEnabled());
        toggles.setSubscriptionEnabled(newToggles.getSubscriptionEnabled());
        toggles.setGiftedSubEnabled(newToggles.getGiftedSubEnabled());
        toggles.setBitsEnabled(newToggles.getBitsEnabled());
        toggles.setRaidEnabled(newToggles.getRaidEnabled());
        toggles.setHostEnabled(newToggles.getHostEnabled());

        return toggleRepository.save(toggles);
    }

    public boolean isEventEnabled(String sessionId, String eventType) {
        EventToggle toggles = getToggles(sessionId);

        return switch (eventType.toUpperCase()) {
            case "FOLLOW" -> toggles.getFollowEnabled();
            case "SUBSCRIPTION" -> toggles.getSubscriptionEnabled();
            case "GIFTED_SUB" -> toggles.getGiftedSubEnabled();
            case "BITS" -> toggles.getBitsEnabled();
            case "RAID" -> toggles.getRaidEnabled();
            case "HOST" -> toggles.getHostEnabled();
            default -> false;
        };
    }
}
