package liraz.echo.controller.api;

import jakarta.validation.Valid;
import liraz.echo.domain.rating.SongRating;
import liraz.echo.service.RatingService;
import liraz.echo.service.RatingStats;
import liraz.echo.service.SongService;
import liraz.echo.dto.rating.RatingRequest;
import liraz.echo.dto.rating.RatingResponse;
import liraz.echo.dto.rating.RatingStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingRestController {

    private final RatingService ratingService;
    private final SongService songService;

    @GetMapping("/{userId}")
    public List<RatingResponse> byUser(@PathVariable Long userId) {
        return ratingService.findByUserId(userId).stream()
                .map(RatingResponse::from)
                .toList();
    }

    @GetMapping("/avg/{songId}")
    public RatingStatsResponse average(@PathVariable Long songId) {
        songService.require(songId);
        RatingStats stats = ratingService.statsForSong(songId);
        return RatingStatsResponse.from(songId, stats);
    }

    @PostMapping("/{songId}")
    public ResponseEntity<RatingResponse> create(@PathVariable Long songId,
                                                 @Valid @RequestBody RatingRequest request,
                                                 Principal principal) {
        SongRating created = ratingService.createRating(
                principal.getName(), songId, request.rating(), request.feeling(), request.review());
        return ResponseEntity
                .created(URI.create("/api/ratings/" + songId))
                .body(RatingResponse.from(created));
    }

    @PutMapping("/{songId}")
    public RatingResponse update(@PathVariable Long songId,
                                 @Valid @RequestBody RatingRequest request,
                                 Principal principal) {
        SongRating updated = ratingService.updateRating(
                principal.getName(), songId, request.rating(), request.feeling(), request.review());
        return RatingResponse.from(updated);
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> delete(@PathVariable Long songId, Principal principal) {
        ratingService.delete(principal.getName(), songId);
        return ResponseEntity.noContent().build();
    }
}