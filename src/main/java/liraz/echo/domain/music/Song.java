package liraz.echo.domain.music;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(
        name = "songs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_songs_album_track",
                columnNames = {"album_id", "track_number"}
        )
)
@Data
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "album_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_songs_album")
    )
    private Album album;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotNull
    @Min(1)
    @Column(name = "track_number", nullable = false)
    private Integer trackNumber;

    @NotNull
    @Min(1)
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Size(max = 10000)
    @Column(length = 10000)
    private String lyrics;

    @URL
    @Size(max = 500)
    @Column(length = 500)
    private String spotifyUrl;

    @NotNull
    @Column(name = "explicit_content", nullable = false)
    private Boolean explicitContent;

    public Song() {
    }

    public Song(Long id, Album album, String title, Integer trackNumber, Integer durationSeconds, String lyrics, String spotifyUrl, Boolean explicitContent) {
        this.id = id;
        this.album = album;
        this.title = title;
        this.trackNumber = trackNumber;
        this.durationSeconds = durationSeconds;
        this.lyrics = lyrics;
        this.spotifyUrl = spotifyUrl;
        this.explicitContent = explicitContent;
    }

    public Song(Album album, String title, Integer trackNumber, Integer durationSeconds, String lyrics, String spotifyUrl, Boolean explicitContent) {
        this.album = album;
        this.title = title;
        this.trackNumber = trackNumber;
        this.durationSeconds = durationSeconds;
        this.lyrics = lyrics;
        this.spotifyUrl = spotifyUrl;
        this.explicitContent = explicitContent;
    }

    @Override
    public String toString() {
        return "Song{id=" + id
                + ", album=" + (album != null ? album.getId() : null)
                + ", title='" + title + "', trackNumber=" + trackNumber
                + ", durationSeconds=" + durationSeconds
                + ", explicitContent=" + explicitContent + "}";
    }
}
