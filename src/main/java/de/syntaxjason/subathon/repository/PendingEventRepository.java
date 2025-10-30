package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingEventRepository extends JpaRepository<PendingEvent, String> {

    List<PendingEvent> findByProcessedFalseOrderByCreatedAtAsc();

    List<PendingEvent> findBySessionIdAndProcessedFalse(String sessionId);
}
