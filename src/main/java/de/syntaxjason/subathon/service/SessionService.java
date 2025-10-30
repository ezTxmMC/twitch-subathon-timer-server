package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.Session;
import de.syntaxjason.subathon.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final Random random = new Random();

    @Transactional
    public Session createSession(String name, String hostUserId, String hostUsername, int initialSeconds) {
        String sessionId = UUID.randomUUID().toString();
        String code = generateSessionCode(); // NEU!

        Session session = Session.builder()
                .sessionId(sessionId)
                .code(code) // NEU!
                .name(name)
                .hostUserId(hostUserId)
                .hostUsername(hostUsername)
                .status(Session.SessionStatus.CREATED)
                .build();

        session = sessionRepository.save(session);
        log.info("Created session: {} with code: {} for host: {}", sessionId, code, hostUsername);

        return session;
    }

    public Session getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    public Session getSessionByCode(String code) {
        return sessionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Session not found with code: " + code));
    }

    @Transactional
    public Session updateStatus(String sessionId, Session.SessionStatus status) {
        Session session = getSession(sessionId);
        session.setStatus(status);
        return sessionRepository.save(session);
    }

    public boolean exists(String sessionId) {
        return sessionRepository.existsBySessionId(sessionId);
    }

    // Generiert 6-stelligen Code (z.B. "A3B7K9")
    private String generateSessionCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Ohne I, O, 0, 1
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Prüfe ob Code schon existiert (unwahrscheinlich aber möglich)
        if (sessionRepository.existsByCode(code.toString())) {
            return generateSessionCode(); // Rekursiv neuen Code generieren
        }

        return code.toString();
    }
}
