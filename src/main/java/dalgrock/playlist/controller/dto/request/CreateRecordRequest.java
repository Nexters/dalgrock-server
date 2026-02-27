package dalgrock.playlist.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

        String location,

        @Schema(description = "기록 날짜(년). 옵셔널. year/month/day 셋 다 있으면 해당 날짜, 하나라도 없으면 오늘. 예: 2026", example = "2026")
        @Min(1900)
        @Max(2100)
        Integer year,

        @Schema(description = "기록 날짜(월). 옵셔널. 1~12", example = "2")
        @Min(1)
        @Max(12)
        Integer month,

        @Schema(description = "기록 날짜(일). 옵셔널. 1~31", example = "21")
        @Min(1)
        @Max(31)
        Integer day
) {

    public record Music(
            @NotNull
            String title,

            @NotNull
            String artist,

            String thumbnail,

            String genre
    ) {
    }
}
