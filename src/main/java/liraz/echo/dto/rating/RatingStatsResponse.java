package liraz.echo.dto.rating;

import liraz.echo.domain.rating.Feeling;
import liraz.echo.service.RatingStats;

public record RatingStatsResponse(
        Long songId,
        Double average,
        Feeling topFeeling,
        long total
) {
    public static RatingStatsResponse from(Long songId, RatingStats stats) {
        return new RatingStatsResponse(
                songId,
                stats.getAverage(),
                stats.getTopFeeling(),
                stats.getTotal()
        );
    }
}
