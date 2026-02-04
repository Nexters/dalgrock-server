package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.infrastructure.repository.dto.GetRecordMusicDto;

public record GetRecordMusicResponse(
        String title,
        String artist,
        String thumbnail
) {

    public static GetRecordMusicResponse from(GetRecordMusicDto dto) {
        return new GetRecordMusicResponse(
                dto.getTitle(),
                dto.getArtist(),
                dto.getThumbnail()
        );
    }
}
