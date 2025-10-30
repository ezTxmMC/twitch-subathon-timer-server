package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.PendingEvent;
import de.syntaxjason.subathon.model.SessionSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventWatcherService {

    private final EventQueueService queueService;
    private final EventToggleService toggleService;
    private final SettingsService settingsService;
    private final TimerService timerService;

    @Scheduled(fixedDelay = 1000) // Check every second
    public void processEvents() {
        List<PendingEvent> pendingEvents = queueService.getPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending events", pendingEvents.size());

        for (PendingEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Error processing event {}", event.getId(), e);
            }
        }
    }

    private void processEvent(PendingEvent event) {
        if (!toggleService.isEventEnabled(event.getSessionId(), event.getEventType())) {
            log.info("Event {} is disabled, skipping", event.getEventType());
            queueService.markAsProcessed(event.getId());
            return;
        }

        SessionSettings settings = settingsService.getSettings(event.getSessionId());
        int secondsToAdd = calculateSeconds(event, settings);

        if (secondsToAdd > 0) {
            String description = buildDescription(event);
            timerService.addTime(event.getSessionId(), secondsToAdd, description);

            log.info("Processed event: {} from {} - Added {}s",
                    event.getEventType(), event.getChannelName(), secondsToAdd);
        }

        queueService.markAsProcessed(event.getId());
    }

    private int calculateSeconds(PendingEvent event, SessionSettings settings) {
        return switch (event.getEventType().toUpperCase()) {
            case "FOLLOW" -> settings.getFollowSeconds();
            case "SUBSCRIPTION" -> calculateSubSeconds(event.getAmount(), settings);
            case "GIFTED_SUB" -> event.getAmount() * settings.getGiftedSubSeconds();
            case "BITS" -> (event.getAmount() / 100) * settings.getBitsPer100();
            case "RAID" -> calculateRaidSeconds(event.getAmount(), settings);
            default -> 0;
        };
    }

    private int calculateSubSeconds(Integer tier, SessionSettings settings) {
        if (tier == null) tier = 1000;

        return switch (tier) {
            case 2000 -> settings.getSubTier2Seconds();
            case 3000 -> settings.getSubTier3Seconds();
            default -> settings.getSubTier1Seconds();
        };
    }

    private int calculateRaidSeconds(Integer viewers, SessionSettings settings) {
        return viewers != null ? viewers * settings.getRaidSecondsPerViewer() : 0;
    }

    private String buildDescription(PendingEvent event) {
        String username = event.getUsername() != null ? event.getUsername() : "Someone";
        String channel = event.getChannelName();

        return switch (event.getEventType().toUpperCase()) {
            case "FOLLOW" -> username + " hat " + channel + " gefolgt!";
            case "SUBSCRIPTION" -> username + " hat " + channel + " abonniert!";
            case "GIFTED_SUB" -> username + " hat " + event.getAmount() + " Subs an " + channel + " verschenkt!";
            case "BITS" -> username + " hat " + event.getAmount() + " Bits zu " + channel + " gecheert!";
            case "RAID" -> username + " hat " + channel + " mit " + event.getAmount() + " Viewern geraided!";
            default -> "Event von " + channel;
        };
    }
}
