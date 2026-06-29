package liraz.echo.controller.api;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Artist;
import liraz.echo.exceptions.ConflictException;
import liraz.echo.service.AlbumService;
import liraz.echo.service.ArtistService;
import liraz.echo.dto.catalog.AlbumRequest;
import liraz.echo.dto.catalog.AlbumResponse;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumRestController {

    private final ArtistService artistService;
    private final AlbumService albumService;

    @PostMapping("/artists/{artistId}")
    public ResponseEntity<AlbumResponse> create(@PathVariable Long artistId,
                                                @Valid @RequestBody AlbumRequest request) {
        Artist artist = artistService.require(artistId);
        if (albumService.titleTakenForArtist(artistId, request.title())) {
            throw new ConflictException("This artist already has an album with that title.");
        }
        Album created = albumService.create(artist, request.toEntity());
        return ResponseEntity
                .created(URI.create("/api/albums/" + created.getId()))
                .body(AlbumResponse.from(created));
    }

    @GetMapping("/artists/{artistId}")
    public List<AlbumResponse> listByArtist(@PathVariable Long artistId) {
        artistService.require(artistId);
        return albumService.findByArtist(artistId).stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AlbumResponse get(@PathVariable Long id) {
        return AlbumResponse.from(albumService.require(id));
    }

    @PutMapping("/{id}")
    public AlbumResponse update(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        Album existing = albumService.require(id);
        Long artistId = existing.getArtist().getId();
        if (albumService.titleTakenByOther(artistId, request.title(), id)) {
            throw new ConflictException("This artist already has another album with that title.");
        }
        Album updated = albumService.update(id, request.toEntity());
        return AlbumResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.require(id);
        if (albumService.hasSongs(id)) {
            throw new ConflictException("This album still has songs and cannot be deleted.");
        }
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}