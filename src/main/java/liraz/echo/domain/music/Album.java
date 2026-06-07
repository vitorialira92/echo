package liraz.echo.domain.music;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(
        name = "albums",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_albums_artist_title",
                columnNames = {"artist_id", "title"}
        )
)
@Data
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
            name = "artist_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_albums_artist")
    )
    private Artist artist;

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

    public Album(Long id, Artist artist, String title, String coverUrl, String spotifyUrl, Integer releaseYear, Genre genre) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.coverUrl = coverUrl;
        this.spotifyUrl = spotifyUrl;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    public Album(Artist artist, String title, String coverUrl, String spotifyUrl, Integer releaseYear, Genre genre) {
        this.artist = artist;
        this.title = title;
        this.coverUrl = coverUrl;
        this.spotifyUrl = spotifyUrl;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "Album{id=" + id
                + ", artist=" + (artist != null ? artist.getId() : null)
                + ", title='" + title + "', genre=" + genre
                + ", releaseYear=" + releaseYear + "}";
    }
}
