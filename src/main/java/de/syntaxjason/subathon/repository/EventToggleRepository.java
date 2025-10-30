package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.EventToggle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventToggleRepository extends JpaRepository<EventToggle, String> {
}
