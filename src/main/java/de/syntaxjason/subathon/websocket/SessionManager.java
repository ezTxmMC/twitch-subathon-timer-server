package de.syntaxjason.subathon.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
public class SessionManager {

    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(String sessionId, WebSocketSession session) {
        sessions.computeIfAbsent(sessionId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket session added for sessionId: {} (total: {})", sessionId, sessions.get(sessionId).size());
    }

    public void removeSession(String sessionId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> sessionSet = sessions.get(sessionId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                sessions.remove(sessionId);
            }
            log.info("WebSocket session removed for sessionId: {}", sessionId);
        }
    }

    public void broadcast(String sessionId, Map<String, Object> data) {
        CopyOnWriteArraySet<WebSocketSession> sessionSet = sessions.get(sessionId);
        if (sessionSet == null || sessionSet.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage message = new TextMessage(json);

            sessionSet.forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    log.error("Failed to send message to WebSocket session", e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to serialize data", e);
        }
    }

    public int getConnectionCount(String sessionId) {
        CopyOnWriteArraySet<WebSocketSession> sessionSet = sessions.get(sessionId);
        return sessionSet != null ? sessionSet.size() : 0;
    }
}
