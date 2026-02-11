package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.model.Record;

public record CreateRecordResponse(Long id) {

    public static CreateRecordResponse from(Record record) {
        return new CreateRecordResponse(record.getId());
    }
}
