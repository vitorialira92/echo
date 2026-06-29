package liraz.echo.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.music.Song;
import liraz.echo.web.Forms;
import org.hibernate.validator.constraints.URL;

public record SongRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull @Min(1) Integer trackNumber,
        @NotNull @Min(1) Integer durationSeconds,
        @Size(max = 10000) String lyrics,
        @URL @Size(max = 500) String spotifyUrl,
        Boolean explicitContent
) {
    public Song toEntity() {
        Song song = new Song();
        song.setTitle(title);
        song.setTrackNumber(trackNumber);
        song.setDurationSeconds(durationSeconds);
        song.setLyrics(Forms.nullIfBlank(lyrics));
        song.setSpotifyUrl(Forms.nullIfBlank(spotifyUrl));
        song.setExplicitContent(explicitContent != null && explicitContent);
        return song;
    }
}
