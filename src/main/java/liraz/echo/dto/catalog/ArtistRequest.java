package liraz.echo.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.music.Artist;
import liraz.echo.domain.music.Country;
import liraz.echo.web.Forms;
import org.hibernate.validator.constraints.URL;

public record ArtistRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Country country,
        @URL @Size(max = 500) String imageUrl
) {
    public Artist toEntity() {
        Artist artist = new Artist();
        artist.setName(name);
        artist.setCountry(country);
        artist.setImageUrl(Forms.nullIfBlank(imageUrl));
        return artist;
    }
}