package dalgrock.playlist.service.dto.command;

import java.util.List;

public record CreateRecordCommand(
        List<CreateRecordMusicCommand> musics,
        List<String> emotions,
        String content,
        List<String> situations,
        String location,
        Integer year,
        Integer month,
        Integer day
) {
}
