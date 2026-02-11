package dalgrock.playlist.infrastructure.client;

import dalgrock.playlist.infrastructure.spotify.dto.MusicCommand;
import java.util.List;

public interface MusicSearchClient {

    List<MusicCommand> search(String keyword);

    String getProviderName();
}
