package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.service.EventQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final EventQueueService queueService;

    @PostMapping("/twitch/follow")
    public ResponseEntity<?> handleFollow(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String channelId = (String) request.get("channelId");
            String channelName = (String) request.get("channelName");
            String username = (String) request.get("username");

            if (sessionId == null || channelId == null || channelName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            queueService.queueEvent(sessionId, channelId, channelName, "FOLLOW", 1, username);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error handling follow event", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/twitch/subscription")
    public ResponseEntity<?> handleSubscription(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String channelId = (String) request.get("channelId");
            String channelName = (String) request.get("channelName");
            String username = (String) request.get("username");
            String tier = (String) request.getOrDefault("tier", "1000");

            if (sessionId == null || channelId == null || channelName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            Integer tierValue = Integer.parseInt(tier);
            queueService.queueEvent(sessionId, channelId, channelName, "SUBSCRIPTION", tierValue, username);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error handling subscription event", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/twitch/gifted-sub")
    public ResponseEntity<?> handleGiftedSub(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String channelId = (String) request.get("channelId");
            String channelName = (String) request.get("channelName");
            String gifterUsername = (String) request.get("gifterUsername");
            Integer amount = (Integer) request.getOrDefault("amount", 1);

            if (sessionId == null || channelId == null || channelName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            queueService.queueEvent(sessionId, channelId, channelName, "GIFTED_SUB", amount, gifterUsername);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error handling gifted sub event", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/twitch/bits")
    public ResponseEntity<?> handleBits(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String channelId = (String) request.get("channelId");
            String channelName = (String) request.get("channelName");
            String username = (String) request.get("username");
            Integer bits = (Integer) request.getOrDefault("bits", 100);

            if (sessionId == null || channelId == null || channelName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            queueService.queueEvent(sessionId, channelId, channelName, "BITS", bits, username);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error handling bits event", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/twitch/raid")
    public ResponseEntity<?> handleRaid(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            String channelId = (String) request.get("channelId");
            String channelName = (String) request.get("channelName");
            String raiderUsername = (String) request.get("raiderUsername");
            Integer viewers = (Integer) request.getOrDefault("viewers", 1);

            if (sessionId == null || channelId == null || channelName == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            queueService.queueEvent(sessionId, channelId, channelName, "RAID", viewers, raiderUsername);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error handling raid event", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
