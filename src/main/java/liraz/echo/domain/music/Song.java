package liraz.echo.domain.music;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(
        name = "songs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_songs_album_track",
                columnNames = {"album_id", "track_number"}
        )
)
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

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

    public Song(Long id, Long albumId, String title, Integer trackNumber, Integer durationSeconds, String lyrics, String spotifyUrl, Boolean explicitContent) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.trackNumber = trackNumber;
        this.durationSeconds = durationSeconds;
        this.lyrics = lyrics;
        this.spotifyUrl = spotifyUrl;
        this.explicitContent = explicitContent;
    }

    public Song(Long albumId, String title, Integer trackNumber, Integer durationSeconds, String lyrics, String spotifyUrl, Boolean explicitContent) {
        this.albumId = albumId;
        this.title = title;
        this.trackNumber = trackNumber;
        this.durationSeconds = durationSeconds;
        this.lyrics = lyrics;
        this.spotifyUrl = spotifyUrl;
        this.explicitContent = explicitContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public Boolean getExplicitContent() {
        return explicitContent;
    }

    public void setExplicitContent(Boolean explicitContent) {
        this.explicitContent = explicitContent;
    }

    @Override
    public String toString() {
        return "Song{id=" + id + ", albumId=" + albumId + ", title='" + title
                + "', trackNumber=" + trackNumber + ", durationSeconds=" + durationSeconds
                + ", explicitContent=" + explicitContent + "}";
    }
}