package dalgrock.playlist.core.exception;

public class SpotifyTokenException extends BusinessException {

    public SpotifyTokenException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
