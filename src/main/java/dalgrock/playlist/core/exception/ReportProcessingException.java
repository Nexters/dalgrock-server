package dalgrock.playlist.core.exception;

public class ReportProcessingException extends BusinessException {

    public ReportProcessingException(Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}
