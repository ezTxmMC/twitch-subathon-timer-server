package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findBySessionId(String sessionId);
    Optional<Session> findByCode(String code); // NEU!
    boolean existsBySessionId(String sessionId);
    boolean existsByCode(String code); // NEU!
}
