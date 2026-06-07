package liraz.echo.domain.rating;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import liraz.echo.domain.music.Song;
import liraz.echo.domain.user.User;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "song_ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_song_ratings_user_song",
                columnNames = {"user_id", "song_id"}
        )
)
@Data
public class SongRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_song_ratings_user")
    )
    private User user;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "song_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_song_ratings_song")
    )
    private Song song;

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

    public SongRating(Long id, User user, Song song, Integer rating, Feeling feeling, String review, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.song = song;
        this.rating = rating;
        this.feeling = feeling;
        this.review = review;
        this.createdAt = createdAt;
    }

    public SongRating(User user, Song song, Integer rating, Feeling feeling, String review) {
        this.user = user;
        this.song = song;
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

    @Override
    public String toString() {
        return "SongRating{id=" + id
                + ", user=" + (user != null ? user.getId() : null)
                + ", song=" + (song != null ? song.getId() : null)
                + ", rating=" + rating + ", feeling=" + feeling
                + ", createdAt=" + createdAt + "}";
    }
}
