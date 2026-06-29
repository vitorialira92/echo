package liraz.echo.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Genre;
import liraz.echo.web.Forms;
import org.hibernate.validator.constraints.URL;

public record AlbumRequest(
        @NotBlank @Size(max = 160) String title,
        @URL @Size(max = 500) String coverUrl,
        @URL @Size(max = 500) String spotifyUrl,
        @NotNull Genre genre,
        @NotNull Integer releaseYear
) {
    public Album toEntity() {
        Album album = new Album();
        album.setTitle(title);
        album.setCoverUrl(Forms.nullIfBlank(coverUrl));
        album.setSpotifyUrl(Forms.nullIfBlank(spotifyUrl));
        album.setGenre(genre);
        album.setReleaseYear(releaseYear);
        return album;
    }
}
