package dalgrock.playlist.service.dto.command;

public record CreateRecordMusicCommand(
        String title,
        String artist,
        String thumbnail,
        String genre
) {
}
