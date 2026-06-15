package liraz.echo.service;

import liraz.echo.domain.rating.Feeling;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RatingStats {

    private final Double average;
    private final Feeling topFeeling;
    private final long total;

    public boolean hasRatings() {
        return total > 0;
    }
}
