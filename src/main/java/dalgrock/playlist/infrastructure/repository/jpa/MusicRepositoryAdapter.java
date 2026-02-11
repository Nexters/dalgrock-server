package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.MusicRepository;
import dalgrock.playlist.model.Music;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MusicRepositoryAdapter implements MusicRepository {

    private final JpaMusicRepository jpaMusicRepository;

    @Override
    public void saveAll(List<Music> musics) {
        jpaMusicRepository.saveAll(musics);
    }

    @Override
    public List<Music> searchByKeyword(String keyword) {
        return jpaMusicRepository.searchByKeyword(keyword);
    }

    @Override
    public boolean existsBySpotifyId(String spotifyId) {
        return jpaMusicRepository.existsBySpotifyId(spotifyId);
    }
}
