package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.Event;
import de.syntaxjason.subathon.model.Timer;
import de.syntaxjason.subathon.repository.TimerRepository;
import de.syntaxjason.subathon.websocket.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {

    private final TimerRepository timerRepository;
    private final EventService eventService;
    private final SessionManager sessionManager;

    @Transactional
    public Timer createTimer(String sessionId, int initialSeconds) {
        Timer timer = Timer.builder()
                .sessionId(sessionId)
                .initialSeconds(initialSeconds)
                .remainingSeconds(initialSeconds)
                .status(Timer.TimerStatus.STOPPED)
                .build();

        timer = timerRepository.save(timer);
        log.info("Created timer for session: {} with {}s", sessionId, initialSeconds);

        return timer;
    }

    public Timer getTimer(String sessionId) {
        return timerRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Timer not found: " + sessionId));
    }

    @Transactional
    public Timer startTimer(String sessionId) {
        Timer timer = getTimer(sessionId);
        timer.setStatus(Timer.TimerStatus.RUNNING);
        timer.setLastTickAt(LocalDateTime.now());
        timer = timerRepository.save(timer);

        log.info("Started timer for session: {}", sessionId);

        eventService.createEvent(sessionId, Event.EventType.TIMER_START, "Timer gestartet", null, null);
        broadcastTimerUpdate(timer);

        return timer;
    }

    @Transactional
    public Timer pauseTimer(String sessionId) {
        Timer timer = getTimer(sessionId);
        timer.setStatus(Timer.TimerStatus.PAUSED);
        timer = timerRepository.save(timer);

        log.info("Paused timer for session: {}", sessionId);

        eventService.createEvent(sessionId, Event.EventType.TIMER_PAUSE, "Timer pausiert", null, null);
        broadcastTimerUpdate(timer);

        return timer;
    }

    @Transactional
    public Timer resetTimer(String sessionId) {
        Timer timer = getTimer(sessionId);
        timer.setRemainingSeconds(timer.getInitialSeconds());
        timer.setStatus(Timer.TimerStatus.STOPPED);
        timer.setLastTickAt(null);
        timer = timerRepository.save(timer);

        log.info("Reset timer for session: {}", sessionId);

        eventService.createEvent(sessionId, Event.EventType.TIMER_RESET, "Timer zurückgesetzt", null, null);
        broadcastTimerUpdate(timer);

        return timer;
    }

    @Transactional
    public Timer addTime(String sessionId, int seconds, String reason) {
        Timer timer = getTimer(sessionId);
        timer.setRemainingSeconds(timer.getRemainingSeconds() + seconds);
        timer = timerRepository.save(timer);

        log.info("Added {}s to timer for session: {} - Reason: {}", seconds, sessionId, reason);

        eventService.createEvent(sessionId, Event.EventType.MANUAL_ADD, reason, seconds, null);
        broadcastTimerUpdate(timer);

        return timer;
    }

    // Scheduled Tick - läuft alle 1000ms
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void tick() {
        timerRepository.findAll().stream()
                .filter(timer -> timer.getStatus() == Timer.TimerStatus.RUNNING)
                .forEach(timer -> {
                    if (timer.getRemainingSeconds() > 0) {
                        timer.setRemainingSeconds(timer.getRemainingSeconds() - 1);
                        timer.setLastTickAt(LocalDateTime.now());
                        timerRepository.save(timer);

                        broadcastTimerUpdate(timer);

                        // Timer abgelaufen
                        if (timer.getRemainingSeconds() == 0) {
                            timer.setStatus(Timer.TimerStatus.STOPPED);
                            timerRepository.save(timer);

                            log.info("Timer ended for session: {}", timer.getSessionId());
                            eventService.createEvent(
                                    timer.getSessionId(),
                                    Event.EventType.TIMER_END,
                                    "Timer abgelaufen",
                                    null,
                                    null
                            );
                        }
                    }
                });
    }

    private void broadcastTimerUpdate(Timer timer) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "TIMER_UPDATE");
        data.put("sessionId", timer.getSessionId());
        data.put("remainingSeconds", timer.getRemainingSeconds());
        data.put("status", timer.getStatus().name());
        data.put("timestamp", LocalDateTime.now().toString());

        sessionManager.broadcast(timer.getSessionId(), data);
    }
}
