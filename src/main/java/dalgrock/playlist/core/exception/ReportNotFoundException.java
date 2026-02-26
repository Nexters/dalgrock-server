package dalgrock.playlist.core.exception;

public class ReportNotFoundException extends BusinessException {

    public ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }
}
