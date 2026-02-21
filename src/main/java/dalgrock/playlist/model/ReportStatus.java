package dalgrock.playlist.model;

public enum ReportStatus {
    CREATED,      // 보고서 생성됨
    PROCESSING,   // AI 분석 중
    COMPLETED,    // 분석 완료
    FAILED        // 분석 실패
}
