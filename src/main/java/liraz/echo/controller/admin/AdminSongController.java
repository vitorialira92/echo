package liraz.echo.controller.admin;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Song;
import liraz.echo.service.AlbumService;
import liraz.echo.service.SongService;
import liraz.echo.web.Forms;
import liraz.echo.web.form.SongForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminSongController {

    private final SongService songService;
    private final AlbumService albumService;

    public AdminSongController(SongService songService, AlbumService albumService) {
        this.songService = songService;
        this.albumService = albumService;
    }

    @GetMapping("/admin/albums/{albumId}/songs")
    public String list(@PathVariable Long albumId, Model model) {
        Album album = albumService.require(albumId);
        model.addAttribute("album", album);
        model.addAttribute("songs", songService.findByAlbum(albumId));
        return "admin/songs";
    }

    @GetMapping("/admin/albums/{albumId}/songs/new")
    public String newForm(@PathVariable Long albumId, Model model) {
        Album album = albumService.require(albumId);
        if (!model.containsAttribute("songForm")) {
            model.addAttribute("songForm", new SongForm());
        }
        model.addAttribute("album", album);
        model.addAttribute("editing", false);
        return "admin/song-form";
    }

    @PostMapping("/admin/albums/{albumId}/songs")
    public String create(@PathVariable Long albumId,
                         @Valid @ModelAttribute("songForm") SongForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Album album = albumService.require(albumId);
        if (form.getTrackNumber() != null
                && songService.trackTakenForAlbum(albumId, form.getTrackNumber())) {
            result.rejectValue("trackNumber", "song.track.taken");
        }
        if (result.hasErrors()) {
            model.addAttribute("album", album);
            model.addAttribute("editing", false);
            return "admin/song-form";
        }
        songService.create(album, toSong(form));
        redirectAttributes.addFlashAttribute("flashSuccess", "song.created");
        return "redirect:/admin/albums/" + albumId + "/songs";
    }

    @GetMapping("/admin/songs/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Song song = songService.require(id);
        if (!model.containsAttribute("songForm")) {
            SongForm form = new SongForm();
            form.setTitle(song.getTitle());
            form.setTrackNumber(song.getTrackNumber());
            form.setDurationSeconds(song.getDurationSeconds());
            form.setLyrics(song.getLyrics());
            form.setSpotifyUrl(song.getSpotifyUrl());
            form.setExplicitContent(song.getExplicitContent());
            model.addAttribute("songForm", form);
        }
        model.addAttribute("album", song.getAlbum());
        model.addAttribute("editing", true);
        model.addAttribute("songId", id);
        return "admin/song-form";
    }

    @PostMapping("/admin/songs/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("songForm") SongForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Song song = songService.require(id);
        Long albumId = song.getAlbum().getId();
        if (form.getTrackNumber() != null
                && songService.trackTakenByOther(albumId, form.getTrackNumber(), id)) {
            result.rejectValue("trackNumber", "song.track.taken");
        }
        if (result.hasErrors()) {
            model.addAttribute("album", song.getAlbum());
            model.addAttribute("editing", true);
            model.addAttribute("songId", id);
            return "admin/song-form";
        }
        songService.update(id, toSong(form));
        redirectAttributes.addFlashAttribute("flashSuccess", "song.updated");
        return "redirect:/admin/albums/" + albumId + "/songs";
    }

    @PostMapping("/admin/songs/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Song song = songService.require(id);
        Long albumId = song.getAlbum().getId();
        songService.delete(id);
        redirectAttributes.addFlashAttribute("flashSuccess", "song.deleted");
        return "redirect:/admin/albums/" + albumId + "/songs";
    }

    private Song toSong(SongForm form) {
        Song song = new Song();
        song.setTitle(form.getTitle());
        song.setTrackNumber(form.getTrackNumber());
        song.setDurationSeconds(form.getDurationSeconds());
        song.setLyrics(Forms.nullIfBlank(form.getLyrics()));
        song.setSpotifyUrl(Forms.nullIfBlank(form.getSpotifyUrl()));
        song.setExplicitContent(form.getExplicitContent());
        return song;
    }
}
