package dalgrock.playlist.model;

import lombok.Getter;

@Getter
public enum SituationValue {

    COMMUTING_TO_WORK(SituationCategory.ROUTINE, "출근길"),
    COMMUTING_FROM_WORK(SituationCategory.ROUTINE, "퇴근길"),
    WAKE_UP(SituationCategory.ROUTINE, "기상했을 때"),
    BEFORE_SLEEP(SituationCategory.ROUTINE, "잠들기 전"),

    AT_WORK(SituationCategory.WORK_STUDY, "업무 중"),
    IN_PROGRESS(SituationCategory.WORK_STUDY, "작업 중"),
    STUDYING(SituationCategory.WORK_STUDY, "공부"),

    HOUSEWORK(SituationCategory.CHORE, "집안일"),
    SHOWERING(SituationCategory.CHORE, "샤워 중"),
    EXERCISING(SituationCategory.CHORE, "운동"),

    WALKING(SituationCategory.LEISURE, "산책"),
    DRIVING(SituationCategory.LEISURE, "드라이브"),
    READING(SituationCategory.LEISURE, "독서"),
    NAP(SituationCategory.LEISURE, "낮잠"),
    DATE(SituationCategory.LEISURE, "데이트"),

    UNKNOWN(SituationCategory.UNKNOWN, "미상");;

    private final SituationCategory category;
    private final String value;

    SituationValue(SituationCategory category, String value) {
        this.category = category;
        this.value = value;
    }

    public static SituationValue fromString(String text) {
        for (SituationValue value : SituationValue.values()) {
            if (value.name().equalsIgnoreCase(text)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
