package liraz.echo.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.music.Country;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ArtistForm {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private Country country;

    @URL
    @Size(max = 500)
    private String imageUrl;
}
