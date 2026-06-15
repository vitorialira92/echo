package liraz.echo.web.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class SongForm {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    @Min(1)
    private Integer trackNumber;

    @NotNull
    @Min(1)
    private Integer durationSeconds;

    @Size(max = 10000)
    private String lyrics;

    @URL
    @Size(max = 500)
    private String spotifyUrl;

    @NotNull
    private Boolean explicitContent = false;
}
