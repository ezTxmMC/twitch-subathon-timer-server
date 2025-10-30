package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.PendingEvent;
import de.syntaxjason.subathon.repository.PendingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventQueueService {

    private final PendingEventRepository pendingEventRepository;

    @Transactional
    public PendingEvent queueEvent(String sessionId, String channelId, String channelName,
                                   String eventType, Integer amount, String username) {

        PendingEvent event = PendingEvent.builder()
                .sessionId(sessionId)
                .channelId(channelId)
                .channelName(channelName)
                .eventType(eventType)
                .amount(amount)
                .username(username)
                .processed(false)
                .createdAt(LocalDateTime.now())
                .build();

        PendingEvent saved = pendingEventRepository.save(event);
        log.info("Queued event: {} from {} (amount: {})", eventType, channelName, amount);

        return saved;
    }

    public List<PendingEvent> getPendingEvents() {
        return pendingEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
    }

    public List<PendingEvent> getPendingEventsBySession(String sessionId) {
        return pendingEventRepository.findBySessionIdAndProcessedFalse(sessionId);
    }

    @Transactional
    public void markAsProcessed(String eventId) {
        pendingEventRepository.findById(eventId).ifPresent(event -> {
            event.setProcessed(true);
            event.setProcessedAt(LocalDateTime.now());
            pendingEventRepository.save(event);
            log.info("Marked event {} as processed", eventId);
        });
    }
}
