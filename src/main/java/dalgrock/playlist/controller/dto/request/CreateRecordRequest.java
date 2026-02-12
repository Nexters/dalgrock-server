package dalgrock.playlist.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRecordRequest(
        @NotNull
        @Size(min = 1)
        List<Music> musics,

        @NotNull
        @Size(min = 1)
        List<String> emotions,

        String content,

        List<String> situations,

        String location
) {

    public record Music(
            @NotNull
            String title,

            @NotNull
            String artist,

            String thumbnail
    ) {
    }
}
