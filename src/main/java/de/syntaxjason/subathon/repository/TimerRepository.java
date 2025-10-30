package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.Timer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Long> {
    Optional<Timer> findBySessionId(String sessionId);
    boolean existsBySessionId(String sessionId);
}
