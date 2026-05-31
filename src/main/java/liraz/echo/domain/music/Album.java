package liraz.echo.domain.music;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(
        name = "albums",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_albums_artist_title",
                columnNames = {"artist_id", "title"}
        )
)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String title;

    @URL
    @Size(max = 500)
    @Column(length = 500)
    private String coverUrl;

    @URL
    @Size(max = 500)
    @Column(length = 500)
    private String spotifyUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genre genre;

    @NotNull
    @Column(nullable = false)
    private Integer releaseYear;

    public Album() {
    }

    public Album(Long id, Long artistId, String title, String coverUrl, String spotifyUrl, Integer releaseYear, Genre genre) {
        this.id = id;
        this.artistId = artistId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.spotifyUrl = spotifyUrl;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    public Album(Long artistId, String title, String coverUrl, String spotifyUrl, Integer releaseYear, Genre genre) {
        this.artistId = artistId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.spotifyUrl = spotifyUrl;
        this.releaseYear = releaseYear;
        this.genre = genre;
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

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Long getId() {
        return id;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @Override
    public String toString() {
        return "Album{id=" + id + ", artistId=" + artistId + ", title='" + title
                + "', genre=" + genre + ", releaseYear=" + releaseYear + "}";
    }
}