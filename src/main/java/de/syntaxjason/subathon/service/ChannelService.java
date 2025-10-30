package de.syntaxjason.subathon.service;

import de.syntaxjason.subathon.model.SessionChannel;
import de.syntaxjason.subathon.repository.SessionChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelService {

    private final SessionChannelRepository channelRepository;

    public List<SessionChannel> getChannels(String sessionId) {
        return channelRepository.findBySessionIdAndActiveTrue(sessionId);
    }

    @Transactional
    public SessionChannel addChannel(String sessionId, String channelName, String channelId, String accessToken) {
        var existingChannel = channelRepository.findBySessionIdAndChannelId(sessionId, channelId);

        if (existingChannel.isPresent()) {
            log.info("Channel {} already in session {}", channelName, sessionId);
            return existingChannel.get();
        }

        SessionChannel channel = SessionChannel.builder()
                .sessionId(sessionId)
                .channelName(channelName)
                .channelId(channelId)
                .accessToken(accessToken)
                .active(true)
                .build();

        SessionChannel saved = channelRepository.save(channel);
        log.info("Added channel {} to session {}", channelName, sessionId);

        return saved;
    }

    @Transactional
    public void removeChannel(String sessionId, String channelId) {
        channelRepository.deleteBySessionIdAndChannelId(sessionId, channelId);
        log.info("Removed channel {} from session {}", channelId, sessionId);
    }

    @Transactional
    public void updateEventSubConnectionId(String channelId, String connectionId) {
        channelRepository.findById(channelId).ifPresent(channel -> {
            channel.setEventSubConnectionId(connectionId);
            channelRepository.save(channel);
            log.info("Updated EventSub connection for channel {}", channel.getChannelName());
        });
    }
}
