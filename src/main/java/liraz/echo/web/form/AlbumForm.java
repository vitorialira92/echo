package liraz.echo.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.music.Genre;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class AlbumForm {

    @NotBlank
    @Size(max = 160)
    private String title;

    @URL
    @Size(max = 500)
    private String coverUrl;

    @URL
    @Size(max = 500)
    private String spotifyUrl;

    @NotNull
    private Genre genre;

    @NotNull
    private Integer releaseYear;
}
