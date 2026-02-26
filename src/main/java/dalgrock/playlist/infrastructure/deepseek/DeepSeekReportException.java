package dalgrock.playlist.infrastructure.deepseek;

/**
 * DeepSeek API 호출 또는 응답 파싱 실패 시 사용합니다.
 */
public class DeepSeekReportException extends RuntimeException {

    public DeepSeekReportException(String message) {
        super(message);
    }

    public DeepSeekReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
