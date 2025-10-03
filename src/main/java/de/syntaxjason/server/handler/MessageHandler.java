package de.syntaxjason.server.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.syntaxjason.server.model.*;
import de.syntaxjason.server.session.SessionManager;
import de.syntaxjason.server.util.LocalDateTimeAdapter;
import io.netty.channel.Channel;

import java.time.LocalDateTime;

public class MessageHandler {
    private final SessionManager sessionManager;
    private final Gson gson;

    public MessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void handleMessage(String sessionId, String channelName, ServerMessage message, Channel channel) {
        String type = message.getType();

        switch (type) {
            case MessageType.SESSION_CREATE -> handleSessionCreate(sessionId, message, channelName, channel);
            case MessageType.SESSION_JOIN -> handleSessionJoin(sessionId, channelName, channel);
            case MessageType.SESSION_LEAVE -> handleSessionLeave(sessionId, channelName);
            case MessageType.TIMER_UPDATE -> handleTimerUpdate(sessionId, message);
            case MessageType.TIMER_PAUSE -> handleTimerPause(sessionId);
            case MessageType.TIMER_RESUME -> handleTimerResume(sessionId);
            case MessageType.EVENT -> handleEvent(sessionId, message);
            case MessageType.CONFIG_UPDATE -> handleConfigUpdate(sessionId, message);
            case MessageType.CHANNEL_ADD -> handleChannelAdd(sessionId, message);
            case MessageType.MULTIPLIER_ACTIVATE -> handleMultiplierActivate(sessionId, message);
            default -> System.out.println("Unknown message type: " + type);
        }
    }

    private void handleSessionCreate(String sessionId, ServerMessage message, String channelName, Channel channel) {
        SessionSyncData syncData = gson.fromJson(gson.toJson(message.getData()), SessionSyncData.class);

        sessionManager.createSession(sessionId, syncData);
        sessionManager.joinSession(sessionId, channelName, channel);

        System.out.println("Session erstellt und joined: " + sessionId + " von " + channelName);
    }

    private void handleSessionJoin(String sessionId, String channelName, Channel channel) {
        sessionManager.joinSession(sessionId, channelName, channel);
    }

    private void handleSessionLeave(String sessionId, String channelName) {
        sessionManager.leaveSession(sessionId, channelName);
    }

    private void handleTimerUpdate(String sessionId, ServerMessage message) {
        TimerStateUpdate update = gson.fromJson(gson.toJson(message.getData()), TimerStateUpdate.class);
        sessionManager.updateTimer(sessionId, update);
    }

    private void handleTimerPause(String sessionId) {
        sessionManager.pauseTimer(sessionId);
    }

    private void handleTimerResume(String sessionId) {
        sessionManager.resumeTimer(sessionId);
    }

    private void handleEvent(String sessionId, ServerMessage message) {
        TimerEvent event = gson.fromJson(gson.toJson(message.getData()), TimerEvent.class);
        sessionManager.addEvent(sessionId, event);
    }

    private void handleConfigUpdate(String sessionId, ServerMessage message) {
        SabathonConfig config = gson.fromJson(gson.toJson(message.getData()), SabathonConfig.class);
        sessionManager.updateConfig(sessionId, config);
    }

    private void handleChannelAdd(String sessionId, ServerMessage message) {
        String channelName = gson.fromJson(gson.toJson(message.getData()), String.class);
        sessionManager.addChannel(sessionId, channelName);
    }

    private void handleMultiplierActivate(String sessionId, ServerMessage message) {
        ActiveMultiplier multiplier = gson.fromJson(gson.toJson(message.getData()), ActiveMultiplier.class);
        sessionManager.activateMultiplier(sessionId, multiplier);
    }
}
