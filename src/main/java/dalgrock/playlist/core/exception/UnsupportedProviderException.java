package dalgrock.playlist.core.exception;

public class UnsupportedProviderException extends BusinessException {

    public UnsupportedProviderException() {
        super(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
    }
}
