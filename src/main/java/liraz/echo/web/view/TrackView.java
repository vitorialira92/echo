package liraz.echo.web.view;

import liraz.echo.domain.music.Song;
import liraz.echo.service.RatingStats;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrackView {
    private final Song song;
    private final RatingStats stats;
}
