package dalgrock.playlist.core.exception;

public class RecordNotFoundException extends BusinessException {

    public RecordNotFoundException() {
        super(ErrorCode.RECORD_NOT_FOUND);
    }
}
