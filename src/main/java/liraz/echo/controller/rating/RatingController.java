package liraz.echo.controller.rating;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Song;
import liraz.echo.service.RatingService;
import liraz.echo.service.SongService;
import liraz.echo.web.form.RatingForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RatingController {

    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String SONG_VIEW = "song";

    private final RatingService ratingService;
    private final SongService songService;

    @PostMapping("/songs/{id}/ratings")
    public String rate(@PathVariable Long id,
                       @Valid @ModelAttribute("ratingForm") RatingForm form,
                       BindingResult result,
                       Principal principal,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        String username = principal.getName();

        if (result.hasErrors()) {
            populateSongPage(id, username, model);
            return SONG_VIEW;
        }

        ratingService.save(
                username,
                id,
                form.getRating(),
                form.getFeeling(),
                form.getReview()
        );

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "rating.saved"
        );

        return "redirect:/songs/" + id;
    }

    @GetMapping("/ratings")
    public String myRatings(Principal principal, Model model) {
        model.addAttribute(
                "ratings",
                ratingService.findByUser(principal.getName())
        );

        return "ratings/list";
    }

    @PostMapping("/ratings/{songId}/delete")
    public String delete(@PathVariable Long songId,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {

        ratingService.delete(
                principal.getName(),
                songId
        );

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "rating.deleted"
        );

        return "redirect:/ratings";
    }

    private void populateSongPage(Long songId,
                                  String username,
                                  Model model) {

        Song song = songService.require(songId);

        model.addAttribute("song", song);
        model.addAttribute("stats", ratingService.statsForSong(songId));
        model.addAttribute(
                "hasOwnRating",
                ratingService.findOwnRating(username, songId).isPresent()
        );
    }
}
