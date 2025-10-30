package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.model.Timer;
import de.syntaxjason.subathon.service.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/timer")
@RequiredArgsConstructor
@Slf4j
public class TimerController {

    private final TimerService timerService;

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<?> startTimer(@PathVariable String sessionId) {
        try {
            Timer timer = timerService.startTimer(sessionId);
            return ResponseEntity.ok(Map.of(
                    "status", timer.getStatus().name(),
                    "remainingSeconds", timer.getRemainingSeconds()
            ));
        } catch (Exception e) {
            log.error("Error starting timer", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<?> pauseTimer(@PathVariable String sessionId) {
        try {
            Timer timer = timerService.pauseTimer(sessionId);
            return ResponseEntity.ok(Map.of(
                    "status", timer.getStatus().name(),
                    "remainingSeconds", timer.getRemainingSeconds()
            ));
        } catch (Exception e) {
            log.error("Error pausing timer", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/reset")
    public ResponseEntity<?> resetTimer(@PathVariable String sessionId) {
        try {
            Timer timer = timerService.resetTimer(sessionId);
            return ResponseEntity.ok(Map.of(
                    "status", timer.getStatus().name(),
                    "remainingSeconds", timer.getRemainingSeconds()
            ));
        } catch (Exception e) {
            log.error("Error resetting timer", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-time")
    public ResponseEntity<?> addTime(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            Integer seconds = (Integer) request.get("seconds");
            String reason = (String) request.get("reason");

            if (sessionId == null || seconds == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            Timer timer = timerService.addTime(sessionId, seconds, reason != null ? reason : "Manuell hinzugefügt");
            return ResponseEntity.ok(Map.of(
                    "status", timer.getStatus().name(),
                    "remainingSeconds", timer.getRemainingSeconds()
            ));
        } catch (Exception e) {
            log.error("Error adding time", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getTimer(@PathVariable String sessionId) {
        try {
            Timer timer = timerService.getTimer(sessionId);
            return ResponseEntity.ok(Map.of(
                    "sessionId", timer.getSessionId(),
                    "status", timer.getStatus().name(),
                    "remainingSeconds", timer.getRemainingSeconds(),
                    "initialSeconds", timer.getInitialSeconds()
            ));
        } catch (Exception e) {
            log.error("Error getting timer", e);
            return ResponseEntity.notFound().build();
        }
    }
}
