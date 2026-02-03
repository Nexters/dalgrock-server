package dalgrock.playlist.core.exception;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(ErrorCode.AUTH_INVALID_TOKEN);
    }
}
