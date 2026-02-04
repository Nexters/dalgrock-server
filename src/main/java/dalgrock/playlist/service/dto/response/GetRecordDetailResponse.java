package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.model.Record;
import java.util.List;

public record GetRecordDetailResponse(
        List<GetRecordMusicResponse> music,
        List<String> emotions,
        String content,
        List<String> situations,
        String location
) {

    public static GetRecordDetailResponse of(Record record, List<GetRecordMusicResponse> recordMusics) {
        return new GetRecordDetailResponse(
                recordMusics,
                record.getEmotionsToString(),
                record.getContent(),
                record.getSituationsToString(),
                record.getLocation()
        );
    }
}
