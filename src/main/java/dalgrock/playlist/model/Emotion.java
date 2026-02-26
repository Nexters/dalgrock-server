package dalgrock.playlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class Emotion {

    @Convert(converter = EmotionValueConverter.class)
    @Column(name = "value", nullable = false)
    private EmotionValue value;

    public static Emotion from(EmotionValue type) {
        return new Emotion(type);
    }

    public String getDisplayValue() {
        return value.getValue();
    }
}
