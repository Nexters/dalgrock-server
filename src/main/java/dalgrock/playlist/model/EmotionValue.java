package dalgrock.playlist.model;

import lombok.Getter;

@Getter
public enum EmotionValue {

    // 들뜬
    HAPPY(EmotionCategory.EXCITED, "행복"),
    JOY(EmotionCategory.EXCITED, "기쁨"),
    FLUTTER(EmotionCategory.EXCITED, "설렘"),
    EXCITED(EmotionCategory.EXCITED, "신남"),
    PROUD(EmotionCategory.EXCITED, "뿌듯함"),
    MOVED(EmotionCategory.EXCITED, "감동"),

    // 가라앉은
    SAD(EmotionCategory.DEPRESSED, "우울"),
    LONGING(EmotionCategory.DEPRESSED, "그리움"),
    LONELY(EmotionCategory.DEPRESSED, "외로움"),
    BORED(EmotionCategory.DEPRESSED, "권태"),
    EMPTY(EmotionCategory.DEPRESSED, "허무"),
    TIRED(EmotionCategory.DEPRESSED, "피곤"),
    REGRET(EmotionCategory.DEPRESSED, "후회"),

    // 날카로운
    ANGER(EmotionCategory.SHARP, "분노"),
    ANXIOUS(EmotionCategory.SHARP, "불안"),
    NERVOUS(EmotionCategory.SHARP, "긴장"),
    JEALOUSY(EmotionCategory.SHARP, "질투"),

    // 복합적인
    LOVE(EmotionCategory.COMPLEX, "사랑"),
    AMBIVALENT(EmotionCategory.COMPLEX, "복잡미묘"),

    // 따뜻한
    GRATEFUL(EmotionCategory.WARM, "감사"),

    // 기타
    UNKNOWN(EmotionCategory.UNKNOWN, "미상"),
    ;

    private final EmotionCategory category;
    private final String value;

    EmotionValue(EmotionCategory category, String value) {
        this.category = category;
        this.value = value;
    }

    public static EmotionValue fromString(String text) {
        if (text == null || text.isBlank()) {
            return UNKNOWN;
        }
        for (EmotionValue e : EmotionValue.values()) {
            if (e.name().equalsIgnoreCase(text) || e.value.equals(text)) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
