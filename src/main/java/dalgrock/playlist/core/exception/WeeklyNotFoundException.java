package dalgrock.playlist.core.exception;

public class WeeklyNotFoundException extends BusinessException {

    public WeeklyNotFoundException(String message) {
        super(ErrorCode.WEEKLY_NOT_FOUND);
    }
}
