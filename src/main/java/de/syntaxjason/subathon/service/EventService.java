package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.Event;
import de.syntaxjason.subathon.repository.EventRepository;
import de.syntaxjason.subathon.websocket.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final SessionManager sessionManager;

    @Transactional
    public Event createEvent(String sessionId, Event.EventType type, String description, Integer secondsAdded, String username) {
        Event event = Event.builder()
                .sessionId(sessionId)
                .type(type)
                .description(description)
                .secondsAdded(secondsAdded)
                .username(username)
                .build();

        event = eventRepository.save(event);
        log.info("Created event: {} for session: {}", type, sessionId);

        broadcastEvent(event);

        return event;
    }

    public List<Event> getEvents(String sessionId) {
        return eventRepository.findBySessionIdOrderByTimestampDesc(sessionId);
    }

    private void broadcastEvent(Event event) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "EVENT");
        data.put("event", Map.of(
                "type", event.getType().name(),
                "description", event.getDescription() != null ? event.getDescription() : "",
                "secondsAdded", event.getSecondsAdded() != null ? event.getSecondsAdded() : 0,
                "username", event.getUsername() != null ? event.getUsername() : "",
                "timestamp", event.getTimestamp().toString()
        ));

        sessionManager.broadcast(event.getSessionId(), data);
    }
}
