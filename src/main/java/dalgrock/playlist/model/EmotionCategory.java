package dalgrock.playlist.model;

import lombok.Getter;

@Getter
public enum EmotionCategory {

    EXCITED("들뜬"),
    DEPRESSED("가라앉은"),
    SHARP("날카로운"),
    COMPLEX("복합적인"),
    WARM("따뜻한"),
    UNKNOWN("미상"),
    ;

    private final String value;

    EmotionCategory(String value) {
        this.value = value;
    }
}
