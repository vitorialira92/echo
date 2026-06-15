package liraz.echo.controller.catalog;

import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Artist;
import liraz.echo.domain.music.Song;
import liraz.echo.domain.rating.SongRating;
import liraz.echo.service.AlbumService;
import liraz.echo.service.ArtistService;
import liraz.echo.service.RatingService;
import liraz.echo.service.SongService;
import liraz.echo.web.form.RatingForm;
import liraz.echo.web.view.TrackView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CatalogController {

    private final ArtistService artistService;
    private final AlbumService albumService;
    private final SongService songService;
    private final RatingService ratingService;

    public CatalogController(ArtistService artistService,
                             AlbumService albumService,
                             SongService songService,
                             RatingService ratingService) {
        this.artistService = artistService;
        this.albumService = albumService;
        this.songService = songService;
        this.ratingService = ratingService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/catalog";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("artists", artistService.search(q));
        model.addAttribute("q", q == null ? "" : q);
        return "catalog";
    }

    @GetMapping("/artists/{id}")
    public String artist(@PathVariable Long id, Model model) {
        Artist artist = artistService.require(id);
        model.addAttribute("artist", artist);
        model.addAttribute("albums", albumService.findByArtist(id));
        return "artist";
    }

    @GetMapping("/albums/{id}")
    public String album(@PathVariable Long id, Model model) {
        Album album = albumService.require(id);
        List<TrackView> tracks = new ArrayList<>();
        for (Song song : songService.findByAlbum(id)) {
            tracks.add(new TrackView(song, ratingService.statsForSong(song.getId())));
        }
        model.addAttribute("album", album);
        model.addAttribute("tracks", tracks);
        return "album";
    }

    @GetMapping("/songs/{id}")
    public String song(@PathVariable Long id, Model model, Principal principal) {
        Song song = songService.require(id);
        model.addAttribute("song", song);
        model.addAttribute("stats", ratingService.statsForSong(id));

        RatingForm form = new RatingForm();
        boolean hasOwnRating = false;
        if (principal != null) {
            Optional<SongRating> own = ratingService.findOwnRating(principal.getName(), id);
            if (own.isPresent()) {
                SongRating rating = own.get();
                form.setRating(rating.getRating());
                form.setFeeling(rating.getFeeling());
                form.setReview(rating.getReview());
                hasOwnRating = true;
            }
        }
        if (!model.containsAttribute("ratingForm")) {
            model.addAttribute("ratingForm", form);
        }
        model.addAttribute("hasOwnRating", hasOwnRating);
        return "song";
    }
}
