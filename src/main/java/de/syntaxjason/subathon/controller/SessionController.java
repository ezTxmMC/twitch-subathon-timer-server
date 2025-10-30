package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.model.Session;
import de.syntaxjason.subathon.service.SessionService;
import de.syntaxjason.subathon.service.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;
    private final TimerService timerService;

    @PostMapping("/create")
    public ResponseEntity<?> createSession(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Integer initialSeconds = (Integer) request.get("initialSeconds");

            if (name == null || initialSeconds == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            String hostUserId = "dummy-user-id";
            String hostUsername = "DummyHost";

            Session session = sessionService.createSession(name, hostUserId, hostUsername, initialSeconds);
            timerService.createTimer(session.getSessionId(), initialSeconds);

            log.info("Created session: {} with code: {}", session.getSessionId(), session.getCode());

            return ResponseEntity.ok(Map.of(
                    "sessionId", session.getSessionId(),
                    "code", session.getCode(), // NEU!
                    "name", session.getName(),
                    "status", session.getStatus().name()
            ));
        } catch (Exception e) {
            log.error("Error creating session", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        try {
            Session session = sessionService.getSession(sessionId);

            return ResponseEntity.ok(Map.of(
                    "sessionId", session.getSessionId(),
                    "code", session.getCode(),
                    "name", session.getName(),
                    "status", session.getStatus().name(),
                    "createdAt", session.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            log.error("Error getting session", e);
            return ResponseEntity.notFound().build();
        }
    }

    // NEU: Join Session mit Code
    @PostMapping("/join")
    public ResponseEntity<?> joinSession(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");

            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Code is required"));
            }

            Session session = sessionService.getSessionByCode(code.toUpperCase());

            return ResponseEntity.ok(Map.of(
                    "sessionId", session.getSessionId(),
                    "code", session.getCode(),
                    "name", session.getName(),
                    "status", session.getStatus().name()
            ));
        } catch (Exception e) {
            log.error("Error joining session with code", e);
            return ResponseEntity.notFound().build();
        }
    }
}
