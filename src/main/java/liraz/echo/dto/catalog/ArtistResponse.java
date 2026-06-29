package liraz.echo.dto.catalog;

import liraz.echo.domain.music.Artist;
import liraz.echo.domain.music.Country;

public record ArtistResponse(
        Long id,
        String name,
        Country country,
        String imageUrl
) {
    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getCountry(),
                artist.getImageUrl()
        );
    }
}
