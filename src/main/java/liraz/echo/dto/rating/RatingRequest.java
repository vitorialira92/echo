package liraz.echo.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.rating.Feeling;

public record RatingRequest(
        @NotNull @Min(1) @Max(10) Integer rating,
        @NotNull Feeling feeling,
        @Size(max = 2000) String review
) { }
