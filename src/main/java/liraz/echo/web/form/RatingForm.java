package liraz.echo.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.rating.Feeling;
import lombok.Data;

@Data
public class RatingForm {

    @NotNull
    @Min(1)
    @Max(10)
    private Integer rating;

    @NotNull
    private Feeling feeling;

    @Size(max = 2000)
    private String review;
}
