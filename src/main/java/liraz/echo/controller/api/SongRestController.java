package liraz.echo.controller.api;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Song;
import liraz.echo.exceptions.ConflictException;
import liraz.echo.service.AlbumService;
import liraz.echo.service.ArtistService;
import liraz.echo.service.SongService;
import liraz.echo.dto.catalog.SongRequest;
import liraz.echo.dto.catalog.SongResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongRestController {

    private final ArtistService artistService;
    private final AlbumService albumService;
    private final SongService songService;

    @PostMapping("/albums/{albumId}")
    public ResponseEntity<SongResponse> create(@PathVariable Long albumId,
                                               @Valid @RequestBody SongRequest request) {
        Album album = albumService.require(albumId);
        if (songService.trackTakenForAlbum(albumId, request.trackNumber())) {
            throw new ConflictException("This album already has a song with that track number.");
        }
        Song created = songService.create(album, request.toEntity());
        return ResponseEntity
                .created(URI.create("/api/songs/" + created.getId()))
                .body(SongResponse.from(created));
    }

    @GetMapping("/albums/{albumId}")
    public List<SongResponse> listByAlbum(@PathVariable Long albumId) {
        albumService.require(albumId);
        return songService.findByAlbum(albumId).stream()
                .map(SongResponse::from)
                .toList();
    }

    @GetMapping("/artists/{artistId}")
    public List<SongResponse> listByArtist(@PathVariable Long artistId) {
        artistService.require(artistId);
        return songService.findByArtist(artistId).stream()
                .map(SongResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SongResponse get(@PathVariable Long id) {
        return SongResponse.from(songService.require(id));
    }

    @PutMapping("/{id}")
    public SongResponse update(@PathVariable Long id, @Valid @RequestBody SongRequest request) {
        Song existing = songService.require(id);
        Long albumId = existing.getAlbum().getId();
        if (songService.trackTakenByOther(albumId, request.trackNumber(), id)) {
            throw new ConflictException("This album already has another song with that track number.");
        }
        Song updated = songService.update(id, request.toEntity());
        return SongResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        songService.require(id);
        songService.delete(id);
        return ResponseEntity.noContent().build();
    }
}