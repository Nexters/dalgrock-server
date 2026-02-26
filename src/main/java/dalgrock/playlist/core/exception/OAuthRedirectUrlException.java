package dalgrock.playlist.core.exception;

public class OAuthRedirectUrlException extends BusinessException {

    public OAuthRedirectUrlException() {
        super(ErrorCode.AUTH_OAUTH_PROCESS_FAILED);
    }
}
