package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.MusicRepository;
import dalgrock.playlist.model.Music;
import java.util.List;
import java.util.Optional;
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
    public Optional<Music> findByArtistAndTitle(String artist, String title) {
        return jpaMusicRepository.findByArtistAndTitle(artist, title);
    }

    @Override
    public Music save(Music music) {
        return jpaMusicRepository.save(music);
    }
}
