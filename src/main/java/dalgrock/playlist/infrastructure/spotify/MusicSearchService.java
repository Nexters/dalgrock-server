package dalgrock.playlist.infrastructure.spotify;

import dalgrock.playlist.infrastructure.client.MusicSearchClient;
import dalgrock.playlist.infrastructure.repository.MusicRepository;
import dalgrock.playlist.infrastructure.spotify.dto.MusicCommand;
import dalgrock.playlist.service.dto.response.MusicSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicSearchService {

    private final MusicSearchClient musicSearchClient;
    private final MusicRepository musicRepository;

    @Cacheable(value = "musicSearch", key = "#keyword")
    @Transactional(readOnly = true)
    public List<MusicSearchResponse> searchTracks(String keyword) {
        String normalizedKeyword = keyword.trim();
        log.info("Searching tracks with keyword: {}", normalizedKeyword);

        List<MusicCommand> dbResults = searchFromDatabase(normalizedKeyword);

        if (!dbResults.isEmpty()) {
            log.info("DB_HIT: Found {} results from Database", dbResults.size());
            return dbResults.stream()
                    .map(MusicSearchResponse::from)
                    .toList();
        }

        log.info("DB_MISS: Calling external provider: {}", musicSearchClient.getProviderName());
        List<MusicCommand> externalResults = musicSearchClient.search(normalizedKeyword);

        log.info("Returning {} results from {}", externalResults.size(), musicSearchClient.getProviderName());
        return externalResults.stream()
                .map(MusicSearchResponse::from)
                .toList();
    }

    private List<MusicCommand> searchFromDatabase(String keyword) {
        return musicRepository.searchByKeyword(keyword).stream()
                .map(MusicCommand::from)
                .toList();
    }
}
