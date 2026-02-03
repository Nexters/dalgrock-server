package dalgrock.playlist.core.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ErrorCode.AUTH_INVALID_TOKEN);
    }
}
