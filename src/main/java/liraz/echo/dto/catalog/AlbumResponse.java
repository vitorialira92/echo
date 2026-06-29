package liraz.echo.dto.catalog;

import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Genre;

public record AlbumResponse(
        Long id,
        Long artistId,
        String title,
        String coverUrl,
        String spotifyUrl,
        Genre genre,
        Integer releaseYear
) {
    public static AlbumResponse from(Album album) {
        return new AlbumResponse(
                album.getId(),
                album.getArtist().getId(),
                album.getTitle(),
                album.getCoverUrl(),
                album.getSpotifyUrl(),
                album.getGenre(),
                album.getReleaseYear()
        );
    }
}

