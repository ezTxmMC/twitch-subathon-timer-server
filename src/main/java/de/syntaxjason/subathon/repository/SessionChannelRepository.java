package de.syntaxjason.subathon.repository;

import de.syntaxjason.subathon.model.SessionChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionChannelRepository extends JpaRepository<SessionChannel, String> {

    List<SessionChannel> findBySessionIdAndActiveTrue(String sessionId);

    Optional<SessionChannel> findBySessionIdAndChannelId(String sessionId, String channelId);

    void deleteBySessionIdAndChannelId(String sessionId, String channelId);
}
