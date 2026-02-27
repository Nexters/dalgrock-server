package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.model.Record;
import java.time.LocalDate;
import java.util.List;

public record GetRecordDetailResponse(
        LocalDate recordDate,
        List<GetRecordMusicResponse> music,
        List<String> emotions,
        String content,
        List<String> situations,
        String location
) {

    public static GetRecordDetailResponse of(LocalDate recordDate, Record record, List<GetRecordMusicResponse> recordMusics) {
        return new GetRecordDetailResponse(
                recordDate,
                recordMusics,
                record.getEmotionsToString(),
                record.getContent(),
                record.getSituationsToString(),
                record.getLocation()
        );
    }
}
