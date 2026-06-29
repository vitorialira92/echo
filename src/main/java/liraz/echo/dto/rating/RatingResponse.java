package liraz.echo.dto.rating;

import liraz.echo.domain.rating.Feeling;
import liraz.echo.domain.rating.SongRating;

import java.time.LocalDateTime;

public record RatingResponse(
        Long id,
        Long userId,
        Long songId,
        Integer rating,
        Feeling feeling,
        String review,
        LocalDateTime createdAt
) {
    public static RatingResponse from(SongRating songRating) {
        return new RatingResponse(
                songRating.getId(),
                songRating.getUser().getId(),
                songRating.getSong().getId(),
                songRating.getRating(),
                songRating.getFeeling(),
                songRating.getReview(),
                songRating.getCreatedAt()
        );
    }
}