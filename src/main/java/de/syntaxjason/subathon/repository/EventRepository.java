package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findBySessionIdOrderByTimestampDesc(String sessionId);
}
