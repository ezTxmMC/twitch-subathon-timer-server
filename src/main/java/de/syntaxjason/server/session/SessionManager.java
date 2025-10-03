package de.syntaxjason.server.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.syntaxjason.server.model.*;
import de.syntaxjason.server.util.LocalDateTimeAdapter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, SessionState> sessions;
    private final Gson gson;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void createSession(String sessionId, SessionSyncData syncData) {
        SessionState state = new SessionState(sessionId, syncData);
        sessions.put(sessionId, state);

        System.out.println("Session erstellt: " + sessionId + " (" + syncData.getSessionName() + ")");
    }

    public SessionState getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public void removeSession(String sessionId) {
        SessionState state = sessions.remove(sessionId);

        if (state != null) {
            System.out.println("Session entfernt: " + sessionId + " (" + state.getSessionName() + ")");
        }
    }

    public void joinSession(String sessionId, String channelName, Channel channel) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            System.err.println("Session nicht gefunden: " + sessionId);
            return;
        }

        state.addParticipant(channelName, channel);

        sendFullSync(sessionId, channel);
        broadcastParticipants(sessionId);
        sendOBSOverlayInfo(sessionId, channel);

        System.out.println(channelName + " ist Session beigetreten: " + sessionId);
    }

    public void leaveSession(String sessionId, String channelName) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.removeParticipant(channelName);
        broadcastParticipants(sessionId);

        System.out.println(channelName + " hat Session verlassen: " + sessionId);

        if (state.getParticipantCount() == 0) {
            removeSession(sessionId);
        }
    }

    public void updateTimer(String sessionId, TimerStateUpdate update) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.setRemainingSeconds(update.getRemainingSeconds());
        state.setPaused(update.isPaused());

        broadcast(sessionId, MessageType.TIMER_UPDATE, update);
    }

    public void addEvent(String sessionId, TimerEvent event) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.addEvent(event);
        broadcast(sessionId, MessageType.EVENT, event);
    }

    public void pauseTimer(String sessionId) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.setPaused(true);
        broadcast(sessionId, MessageType.TIMER_PAUSE, null);
    }

    public void resumeTimer(String sessionId) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.setPaused(false);
        broadcast(sessionId, MessageType.TIMER_RESUME, null);
    }

    public void updateConfig(String sessionId, SabathonConfig config) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.setConfig(config);
        broadcast(sessionId, MessageType.CONFIG_UPDATE, config);
    }

    public void addChannel(String sessionId, String channelName) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        broadcast(sessionId, MessageType.CHANNEL_ADD, channelName);
    }

    public void activateMultiplier(String sessionId, ActiveMultiplier multiplier) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        state.setActiveMultiplier(multiplier);
        broadcast(sessionId, MessageType.MULTIPLIER_ACTIVATE, multiplier);
    }

    private void sendFullSync(String sessionId, Channel channel) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        SessionSyncData syncData = state.toSyncData();
        ServerMessage message = new ServerMessage(MessageType.FULL_SYNC, "server", syncData);
        String json = gson.toJson(message);

        channel.writeAndFlush(new TextWebSocketFrame(json));
    }

    private void sendOBSOverlayInfo(String sessionId, Channel channel) {
        OBSOverlayInfo info = new OBSOverlayInfo(
                sessionId,
                "http://localhost:8080/overlay/" + sessionId
        );

        ServerMessage message = new ServerMessage("OBS_OVERLAY_INFO", "server", info);
        String json = gson.toJson(message);

        channel.writeAndFlush(new TextWebSocketFrame(json));
    }

    private void broadcastParticipants(String sessionId) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        List<ParticipantInfo> participants = state.getParticipants().values().stream().toList();
        broadcast(sessionId, MessageType.PARTICIPANTS, participants);
    }

    private void broadcast(String sessionId, String messageType, Object data) {
        SessionState state = getSession(sessionId);

        if (state == null) {
            return;
        }

        ServerMessage message = new ServerMessage(messageType, "server", data);
        String json = gson.toJson(message);

        for (Channel channel : state.getAllChannels()) {
            if (channel.isActive()) {
                TextWebSocketFrame frame = new TextWebSocketFrame(json);
                channel.writeAndFlush(frame);
            }
        }
    }


    public Collection<SessionState> getAllSessions() {
        return sessions.values();
    }
}
