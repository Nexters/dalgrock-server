package dalgrock.playlist.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "emotions",
            joinColumns = @JoinColumn(name = "record_id")
    )
    @Builder.Default
    private List<Emotion> emotions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "situations",
            joinColumns = @JoinColumn(name = "record_id")
    )
    @Builder.Default
    private List<Situation> situations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_id", nullable = false)
    private Weekly weekly;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public List<String> getEmotionsToString() {
        return this.emotions.stream()
                .map(Emotion::getDisplayValue)
                .toList();
    }

    public List<String> getSituationsToString() {
        return this.situations.stream()
                .map(Situation::getDisplayValue)
                .toList();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateEmotions(List<Emotion> emotions) {
        this.emotions.clear();
        this.emotions.addAll(emotions);
    }

    public void updateSituations(List<Situation> situations) {
        this.situations.clear();
        this.situations.addAll(situations);
    }

    public void updateThumbnail(String thumbnail) {
        this.thumbnail = thumbnail != null ? thumbnail : "";
    }

    public void softDelete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
