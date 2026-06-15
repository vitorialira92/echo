package liraz.echo.controller.admin;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Artist;
import liraz.echo.service.AlbumService;
import liraz.echo.service.ArtistService;
import liraz.echo.web.Forms;
import liraz.echo.web.form.ArtistForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/artists")
@RequiredArgsConstructor
public class AdminArtistController {

    private static final String FORM_VIEW = "admin/artist-form";
    private static final String LIST_VIEW = "admin/artists";

    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    private final ArtistService artistService;
    private final AlbumService albumService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("artists", artistService.findAll());
        return LIST_VIEW;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("artistForm")) {
            model.addAttribute("artistForm", new ArtistForm());
        }

        populateCreateForm(model);

        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("artistForm") ArtistForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            populateCreateForm(model);
            return FORM_VIEW;
        }

        artistService.create(toArtist(form));

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "artist.created"
        );

        return "redirect:/admin/artists";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Artist artist = artistService.require(id);

        if (!model.containsAttribute("artistForm")) {
            model.addAttribute("artistForm", toForm(artist));
        }

        populateEditForm(model, id);

        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("artistForm") ArtistForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            populateEditForm(model, id);
            return FORM_VIEW;
        }

        artistService.update(id, toArtist(form));

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "artist.updated"
        );

        return "redirect:/admin/artists";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {

        if (albumService.hasAlbums(id)) {
            redirectAttributes.addFlashAttribute(
                    FLASH_ERROR,
                    "artist.delete.hasAlbums"
            );

            return "redirect:/admin/artists";
        }

        artistService.delete(id);

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "artist.deleted"
        );

        return "redirect:/admin/artists";
    }

    private void populateCreateForm(Model model) {
        model.addAttribute("editing", false);
    }

    private void populateEditForm(Model model, Long artistId) {
        model.addAttribute("editing", true);
        model.addAttribute("artistId", artistId);
    }

    private ArtistForm toForm(Artist artist) {
        ArtistForm form = new ArtistForm();

        form.setName(artist.getName());
        form.setCountry(artist.getCountry());
        form.setImageUrl(artist.getImageUrl());

        return form;
    }

    private Artist toArtist(ArtistForm form) {
        Artist artist = new Artist();

        artist.setName(form.getName());
        artist.setCountry(form.getCountry());
        artist.setImageUrl(
                Forms.nullIfBlank(form.getImageUrl())
        );

        return artist;
    }
}