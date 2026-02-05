package dalgrock.playlist.service;

import dalgrock.playlist.core.exception.ErrorCode;
import dalgrock.playlist.core.exception.RecordNotFoundException;
import dalgrock.playlist.core.exception.UnauthorizedException;
import dalgrock.playlist.infrastructure.repository.RecordMusicRepository;
import dalgrock.playlist.infrastructure.repository.RecordRepository;
import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;
import dalgrock.playlist.model.Record;
import dalgrock.playlist.service.dto.response.GetRecordDetailResponse;
import dalgrock.playlist.service.dto.response.GetRecordMusicResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final RecordMusicRepository recordMusicRepository;

    public GetRecordDetailResponse getRecordDetail(Long userId, Long recordId) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND.getMessage()));

        if (!record.getUserId().equals(userId)) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN.getMessage());
        }

        List<GetRecordMusicDto> recordMusics = recordMusicRepository.findAllByRecordId(record.getId());

        List<GetRecordMusicResponse> recordMusicResponses = recordMusics.stream()
                .map(GetRecordMusicResponse::from)
                .toList();

        return GetRecordDetailResponse.of(record, recordMusicResponses);
    }
}
