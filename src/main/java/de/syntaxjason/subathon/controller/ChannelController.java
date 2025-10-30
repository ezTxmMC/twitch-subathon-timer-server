package de.syntaxjason.subathon.controller;

import de.syntaxjason.subathon.model.SessionChannel;
import de.syntaxjason.subathon.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Slf4j
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<List<SessionChannel>> getChannels(@PathVariable String sessionId) {
        try {
            List<SessionChannel> channels = channelService.getChannels(sessionId);
            return ResponseEntity.ok(channels);
        } catch (Exception e) {
            log.error("Error getting channels", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{sessionId}/add")
    public ResponseEntity<SessionChannel> addChannel(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        try {
            String channelName = request.get("channelName");
            String channelId = request.get("channelId");
            String accessToken = request.get("accessToken");

            if (channelName == null || channelId == null || accessToken == null) {
                return ResponseEntity.badRequest().build();
            }

            SessionChannel channel = channelService.addChannel(sessionId, channelName, channelId, accessToken);
            return ResponseEntity.ok(channel);
        } catch (Exception e) {
            log.error("Error adding channel", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{sessionId}/remove/{channelId}")
    public ResponseEntity<?> removeChannel(
            @PathVariable String sessionId,
            @PathVariable String channelId) {
        try {
            channelService.removeChannel(sessionId, channelId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Error removing channel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
