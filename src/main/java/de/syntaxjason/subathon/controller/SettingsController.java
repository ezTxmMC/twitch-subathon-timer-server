package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.model.SessionSettings;
import de.syntaxjason.subathon.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionSettings> getSettings(@PathVariable String sessionId) {
        try {
            SessionSettings settings = settingsService.getSettings(sessionId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("Error getting settings", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<SessionSettings> updateSettings(
            @PathVariable String sessionId,
            @RequestBody SessionSettings settings) {
        try {
            SessionSettings updated = settingsService.updateSettings(sessionId, settings);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating settings", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
