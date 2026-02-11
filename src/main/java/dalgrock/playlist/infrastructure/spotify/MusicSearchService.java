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
        log.info("음악 검색 시작: 키워드={}", normalizedKeyword);

        List<MusicCommand> dbResults = searchFromDatabase(normalizedKeyword);

        if (!dbResults.isEmpty()) {
            log.info("DB_HIT: 데이터베이스에서 {}개 결과 발견", dbResults.size());
            return dbResults.stream()
                    .map(MusicSearchResponse::from)
                    .toList();
        }

        log.info("DB_MISS: 외부 API 호출 - 제공자={}", musicSearchClient.getProviderName());
        List<MusicCommand> externalResults = musicSearchClient.search(normalizedKeyword);

        log.info("{}에서 {}개 결과 반환", musicSearchClient.getProviderName(), externalResults.size());
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
