package dalgrock.playlist.controller;

import dalgrock.playlist.infrastructure.spotify.SpotifySearchService;
import dalgrock.playlist.service.dto.response.MusicSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Music", description = "음악 검색 API")
@SecurityRequirement(name = "cookieAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/musics")
public class MusicControllerV1 {

    private final SpotifySearchService spotifySearchService;

    @Operation(summary = "음악 검색", description = "키워드로 음악을 검색합니다")
    @GetMapping("/search")
    public List<MusicSearchResponse> searchMusic(@RequestParam String keyword) {
        return spotifySearchService.searchTracks(keyword);
    }
}
