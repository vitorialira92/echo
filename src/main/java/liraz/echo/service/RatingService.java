package liraz.echo.service;

import liraz.echo.domain.music.Song;
import liraz.echo.domain.rating.Feeling;
import liraz.echo.domain.rating.SongRating;
import liraz.echo.domain.user.User;
import liraz.echo.repository.SongRatingRepository;
import liraz.echo.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RatingService {

    private final SongRatingRepository songRatingRepository;
    private final UserService userService;
    private final SongService songService;

    public RatingService(SongRatingRepository songRatingRepository,
                         UserService userService,
                         SongService songService) {
        this.songRatingRepository = songRatingRepository;
        this.userService = userService;
        this.songService = songService;
    }

    @Transactional(readOnly = true)
    public List<SongRating> findByUser(String username) {
        User user = userService.requireByUsername(username);
        return songRatingRepository.findByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public Optional<SongRating> findOwnRating(String username, Long songId) {
        User user = userService.requireByUsername(username);
        return songRatingRepository.findByUserIdAndSongId(user.getId(), songId);
    }

    @Transactional(readOnly = true)
    public RatingStats statsForSong(Long songId) {
        List<SongRating> ratings = songRatingRepository.findBySongId(songId);
        if (ratings.isEmpty()) {
            return new RatingStats(null, null, 0);
        }
        double average = ratings.stream().mapToInt(SongRating::getRating).average().orElse(0.0);
        Map<Feeling, Long> counts = ratings.stream()
                .collect(Collectors.groupingBy(SongRating::getFeeling, Collectors.counting()));
        Feeling top = counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        return new RatingStats(average, top, ratings.size());
    }

    public void save(String username, Long songId, Integer rating, Feeling feeling, String review) {
        User user = userService.requireByUsername(username);
        Song song = songService.require(songId);
        SongRating songRating = songRatingRepository
                .findByUserIdAndSongId(user.getId(), song.getId())
                .orElseGet(() -> new SongRating(user, song, rating, feeling, review));
        songRating.setRating(rating);
        songRating.setFeeling(feeling);
        songRating.setReview(review);
        songRatingRepository.save(songRating);
    }

    public void delete(String username, Long songId) {
        SongRating songRating = findOwnRating(username, songId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for song: " + songId));
        songRatingRepository.delete(songRating);
    }
}
