package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.model.EventToggle;
import de.syntaxjason.subathon.service.EventToggleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event-toggles")
@RequiredArgsConstructor
@Slf4j
public class EventToggleController {

    private final EventToggleService toggleService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<EventToggle> getToggles(@PathVariable String sessionId) {
        try {
            EventToggle toggles = toggleService.getToggles(sessionId);
            return ResponseEntity.ok(toggles);
        } catch (Exception e) {
            log.error("Error getting toggles", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<EventToggle> updateToggles(
            @PathVariable String sessionId,
            @RequestBody EventToggle toggles) {
        try {
            EventToggle updated = toggleService.updateToggles(sessionId, toggles);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating toggles", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
