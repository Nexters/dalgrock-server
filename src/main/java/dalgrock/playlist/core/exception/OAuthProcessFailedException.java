package dalgrock.playlist.core.exception;

public class OAuthProcessFailedException extends BusinessException {

    public OAuthProcessFailedException() {
        super(ErrorCode.AUTH_OAUTH_PROCESS_FAILED);
    }
}
