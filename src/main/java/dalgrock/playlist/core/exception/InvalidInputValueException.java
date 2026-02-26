package dalgrock.playlist.core.exception;

public class InvalidInputValueException extends BusinessException {

    public InvalidInputValueException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}
