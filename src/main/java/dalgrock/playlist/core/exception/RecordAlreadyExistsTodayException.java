package dalgrock.playlist.core.exception;

public class RecordAlreadyExistsTodayException extends BusinessException {

    public RecordAlreadyExistsTodayException() {
        super(ErrorCode.RECORD_ALREADY_EXISTS_TODAY);
    }
}
