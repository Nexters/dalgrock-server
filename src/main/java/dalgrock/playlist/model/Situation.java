package dalgrock.playlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class Situation {

    @Enumerated(EnumType.STRING)
    @Column(name = "value", nullable = false)
    private SituationValue value;

    public static Situation from(SituationValue value) {
        return new Situation(value);
    }

    public String getDisplayValue() {
        return value.getValue();
    }
}
