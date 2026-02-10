package dalgrock.playlist.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U002", "사용자 저장에 실패했습니다."),

    // Auth
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 인증 토큰입니다."),
    AUTH_OAUTH_PROCESS_FAILED(HttpStatus.UNAUTHORIZED, "A002", "OAuth2 인증 처리 중 오류가 발생했습니다."),
    AUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "A003", "지원하지 않는 소셜 로그인 제공자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다."),

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다."),

    // Record
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "내 기록을 찾을 수 없습니다."),

    // Weekly
    WEEKLY_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "주차를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
