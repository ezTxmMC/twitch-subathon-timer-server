package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.SessionSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionSettingsRepository extends JpaRepository<SessionSettings, String> {
}
