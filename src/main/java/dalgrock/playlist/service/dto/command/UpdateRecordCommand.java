package dalgrock.playlist.service.dto.command;

import java.util.List;

public record UpdateRecordCommand(
        String type,
        List<CreateRecordMusicCommand> musics,
        List<String> emotions,
        List<String> situations,
        String content
) {
}
