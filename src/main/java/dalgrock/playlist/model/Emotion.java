package dalgrock.playlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Embeddable
public class Emotion {

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "value", nullable = false)
    private String value;
}
