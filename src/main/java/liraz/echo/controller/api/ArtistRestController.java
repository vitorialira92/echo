package liraz.echo.controller.api;

import jakarta.validation.Valid;
import liraz.echo.domain.music.Artist;
import liraz.echo.exceptions.ConflictException;
import liraz.echo.service.AlbumService;
import liraz.echo.service.ArtistService;
import liraz.echo.dto.catalog.ArtistRequest;
import liraz.echo.dto.catalog.ArtistResponse;
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
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistRestController {

    private final ArtistService artistService;
    private final AlbumService albumService;

    @GetMapping
    public List<ArtistResponse> list() {
        return artistService.findAll().stream()
                .map(ArtistResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ArtistResponse get(@PathVariable Long id) {
        return ArtistResponse.from(artistService.require(id));
    }

    @PostMapping
    public ResponseEntity<ArtistResponse> create(@Valid @RequestBody ArtistRequest request) {
        Artist created = artistService.create(request.toEntity());
        return ResponseEntity
                .created(URI.create("/api/artists/" + created.getId()))
                .body(ArtistResponse.from(created));
    }

    @PutMapping("/{id}")
    public ArtistResponse update(@PathVariable Long id, @Valid @RequestBody ArtistRequest request) {
        Artist updated = artistService.update(id, request.toEntity());
        return ArtistResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.require(id);
        if (albumService.hasAlbums(id)) {
            throw new ConflictException("This artist still has albums and cannot be deleted.");
        }
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}