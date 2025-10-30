package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.model.Event;
import de.syntaxjason.subathon.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getEvents(@PathVariable String sessionId) {
        try {
            List<Event> events = eventService.getEvents(sessionId);

            List<Map<String, Object>> eventList = events.stream()
                    .map(event -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", event.getType().name());
                        map.put("description", event.getDescription() != null ? event.getDescription() : "");
                        map.put("secondsAdded", event.getSecondsAdded() != null ? event.getSecondsAdded() : 0);
                        map.put("username", event.getUsername() != null ? event.getUsername() : "");
                        map.put("timestamp", event.getTimestamp().toString());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(eventList);
        } catch (Exception e) {
            log.error("Error getting events", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
