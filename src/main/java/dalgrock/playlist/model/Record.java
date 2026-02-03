package dalgrock.playlist.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity(name = "records")
@Table(name = "records")
public class Record extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String thumbnail;
    private String location;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "music_id", nullable = false)
    private Long musicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id", nullable = false)
    private Emotion emotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private Situation situation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_id", nullable = false)
    private Weekly weekly;
}
