package dalgrock.playlist.infrastructure.spotify;

import dalgrock.playlist.infrastructure.repository.MusicRepository;
import dalgrock.playlist.model.Music;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusicAsyncStorage {

    private final MusicRepository musicRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTracks(List<Music> tracks) {
        try {
            log.info("Async saving {} tracks to database", tracks.size());

            List<Music> newTracks = tracks.stream()
                    .filter(track -> track.getSpotifyId() != null)
                    .filter(track -> !musicRepository.existsBySpotifyId(track.getSpotifyId()))
                    .collect(Collectors.toList());

            if (!newTracks.isEmpty()) {
                musicRepository.saveAll(newTracks);
                log.info("Successfully saved {} new tracks", newTracks.size());
            }
        } catch (Exception e) {
            log.error("Failed to save tracks asynchronously", e);
        }
    }
}
