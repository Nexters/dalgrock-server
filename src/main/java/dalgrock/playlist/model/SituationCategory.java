package dalgrock.playlist.model;

import lombok.Getter;

@Getter
public enum SituationCategory {

    ROUTINE("일상/이동"),
    WORK_STUDY("생산성"),
    CHORE("활동/가사"),
    LEISURE("휴식/관계"),
    UNKNOWN("미상")
    ;

    private final String value;

    SituationCategory(String value) {
        this.value = value;
    }
}
