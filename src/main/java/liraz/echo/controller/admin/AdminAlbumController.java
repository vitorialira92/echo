package liraz.echo.controller.admin;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Artist;
import liraz.echo.service.AlbumService;
import liraz.echo.service.ArtistService;
import liraz.echo.web.Forms;
import liraz.echo.web.form.AlbumForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminAlbumController {
    private static final String FORM_VIEW = "admin/album-form";
    private static final String LIST_VIEW = "admin/albums";

    private static final String FLASH_SUCCESS = "flashSuccess";
    private static final String FLASH_ERROR = "flashError";

    private final AlbumService albumService;
    private final ArtistService artistService;

    @GetMapping("/admin/artists/{artistId}/albums")
    public String list(@PathVariable Long artistId, Model model) {
        Artist artist = artistService.require(artistId);

        model.addAttribute("artist", artist);
        model.addAttribute("albums", albumService.findByArtist(artistId));

        return LIST_VIEW;
    }

    @GetMapping("/admin/artists/{artistId}/albums/new")
    public String newForm(@PathVariable Long artistId, Model model) {
        Artist artist = artistService.require(artistId);

        if (!model.containsAttribute("albumForm")) {
            model.addAttribute("albumForm", new AlbumForm());
        }

        populateCreateForm(model, artist);

        return FORM_VIEW;
    }

    @PostMapping("/admin/artists/{artistId}/albums")
    public String create(@PathVariable Long artistId,
                         @Valid @ModelAttribute("albumForm") AlbumForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Artist artist = artistService.require(artistId);

        validateCreate(artistId, form, result);

        if (result.hasErrors()) {
            populateCreateForm(model, artist);
            return FORM_VIEW;
        }

        albumService.create(artist, toAlbum(form));

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "album.created"
        );

        return "redirect:/admin/artists/" + artistId + "/albums";
    }

    @GetMapping("/admin/albums/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Album album = albumService.require(id);

        if (!model.containsAttribute("albumForm")) {
            model.addAttribute("albumForm", toForm(album));
        }

        populateEditForm(model, album);

        return FORM_VIEW;
    }

    @PostMapping("/admin/albums/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("albumForm") AlbumForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Album album = albumService.require(id);
        Long artistId = album.getArtist().getId();

        validateUpdate(artistId, id, form, result);

        if (result.hasErrors()) {
            populateEditForm(model, album);
            return FORM_VIEW;
        }

        albumService.update(id, toAlbum(form));

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "album.updated"
        );

        return "redirect:/admin/artists/" + artistId + "/albums";
    }

    @PostMapping("/admin/albums/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {

        Album album = albumService.require(id);
        Long artistId = album.getArtist().getId();

        if (albumService.hasSongs(id)) {
            redirectAttributes.addFlashAttribute(
                    FLASH_ERROR,
                    "album.delete.hasSongs"
            );

            return "redirect:/admin/artists/" + artistId + "/albums";
        }

        albumService.delete(id);

        redirectAttributes.addFlashAttribute(
                FLASH_SUCCESS,
                "album.deleted"
        );

        return "redirect:/admin/artists/" + artistId + "/albums";
    }

    private void populateCreateForm(Model model, Artist artist) {
        model.addAttribute("artist", artist);
        model.addAttribute("editing", false);
    }

    private void populateEditForm(Model model, Album album) {
        model.addAttribute("artist", album.getArtist());
        model.addAttribute("editing", true);
        model.addAttribute("albumId", album.getId());
    }

    private void validateCreate(Long artistId,
                                AlbumForm form,
                                BindingResult result) {

        if (form.getTitle() != null
                && albumService.titleTakenForArtist(
                artistId,
                form.getTitle())) {

            result.rejectValue(
                    "title",
                    "album.title.taken"
            );
        }
    }

    private void validateUpdate(Long artistId,
                                Long albumId,
                                AlbumForm form,
                                BindingResult result) {

        if (form.getTitle() != null
                && albumService.titleTakenByOther(
                artistId,
                form.getTitle(),
                albumId)) {

            result.rejectValue(
                    "title",
                    "album.title.taken"
            );
        }
    }

    private AlbumForm toForm(Album album) {
        AlbumForm form = new AlbumForm();

        form.setTitle(album.getTitle());
        form.setCoverUrl(album.getCoverUrl());
        form.setSpotifyUrl(album.getSpotifyUrl());
        form.setGenre(album.getGenre());
        form.setReleaseYear(album.getReleaseYear());

        return form;
    }

    private Album toAlbum(AlbumForm form) {
        Album album = new Album();

        album.setTitle(form.getTitle());
        album.setCoverUrl(Forms.nullIfBlank(form.getCoverUrl()));
        album.setSpotifyUrl(Forms.nullIfBlank(form.getSpotifyUrl()));
        album.setGenre(form.getGenre());
        album.setReleaseYear(form.getReleaseYear());

        return album;
    }
}