package liraz.echo.domain.rating;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "song_ratings")
@IdClass(SongRatingId.class)
public class SongRating {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "song_id", nullable = false)
    private Long songId;

    @NotNull
    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer rating;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Feeling feeling;

    @Size(max = 2000)
    @Column(length = 2000)
    private String review;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SongRating() {
    }

    public SongRating(Long userId, Long songId, Integer rating, Feeling feeling, String review, LocalDateTime createdAt) {
        this.userId = userId;
        this.songId = songId;
        this.rating = rating;
        this.feeling = feeling;
        this.review = review;
        this.createdAt = createdAt;
    }

    public SongRating(Long userId, Long songId, Integer rating, Feeling feeling, String review) {
        this.userId = userId;
        this.songId = songId;
        this.rating = rating;
        this.feeling = feeling;
        this.review = review;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Feeling getFeeling() {
        return feeling;
    }

    public void setFeeling(Feeling feeling) {
        this.feeling = feeling;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    public Integer getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "SongRating{userId=" + userId + ", songId=" + songId + ", rating=" + rating
                + ", feeling=" + feeling + ", createdAt=" + createdAt + "}";
    }
}
