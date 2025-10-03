package de.syntaxjason.server.model;

import io.netty.channel.Channel;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionState {
    private final String sessionId;
    private String sessionName;
    private long initialMinutes;
    private long remainingSeconds;
    private boolean isPaused;
    private LocalDateTime createdAt;
    private SabathonConfig config;
    private Session currentSession;
    private ActiveMultiplier activeMultiplier;
    private final List<TimerEvent> eventHistory;
    private final Map<String, ParticipantInfo> participants;
    private final Map<String, Channel> clientChannels;

    public SessionState(String sessionId, SessionSyncData syncData) {
        this.sessionId = sessionId;
        this.sessionName = syncData.getSessionName();
        this.initialMinutes = syncData.getInitialMinutes();
        this.remainingSeconds = syncData.getRemainingMinutes() * 60;
        this.isPaused = syncData.isPaused();
        this.createdAt = LocalDateTime.now();
        this.config = syncData.getConfig();
        this.currentSession = syncData.getCurrentSession();
        this.activeMultiplier = syncData.getActiveMultiplier();
        this.eventHistory = new ArrayList<>(syncData.getEventHistory() != null ? syncData.getEventHistory() : new ArrayList<>());
        this.participants = new ConcurrentHashMap<>();
        this.clientChannels = new ConcurrentHashMap<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public long getInitialMinutes() {
        return initialMinutes;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SabathonConfig getConfig() {
        return config;
    }

    public void setConfig(SabathonConfig config) {
        this.config = config;
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public ActiveMultiplier getActiveMultiplier() {
        return activeMultiplier;
    }

    public void setActiveMultiplier(ActiveMultiplier activeMultiplier) {
        this.activeMultiplier = activeMultiplier;
    }

    public List<TimerEvent> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }

    public void addEvent(TimerEvent event) {
        eventHistory.add(event);
    }

    public Map<String, ParticipantInfo> getParticipants() {
        return new HashMap<>(participants);
    }

    public void addParticipant(String channelName, Channel channel) {
        ParticipantInfo info = new ParticipantInfo(channelName);
        participants.put(channelName, info);
        clientChannels.put(channelName, channel);
    }

    public void removeParticipant(String channelName) {
        participants.remove(channelName);
        clientChannels.remove(channelName);
    }

    public Channel getParticipantChannel(String channelName) {
        return clientChannels.get(channelName);
    }

    public Collection<Channel> getAllChannels() {
        return new ArrayList<>(clientChannels.values());
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public SessionSyncData toSyncData() {
        SessionSyncData syncData = new SessionSyncData();
        syncData.setSessionId(sessionId);
        syncData.setSessionName(sessionName);
        syncData.setInitialMinutes(initialMinutes);
        syncData.setRemainingMinutes(remainingSeconds / 60);
        syncData.setPaused(isPaused);
        syncData.setConfig(config);
        syncData.setCurrentSession(currentSession);
        syncData.setActiveMultiplier(activeMultiplier);
        syncData.setEventHistory(new ArrayList<>(eventHistory));
        return syncData;
    }
}
