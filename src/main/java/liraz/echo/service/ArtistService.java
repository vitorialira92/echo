package liraz.echo.service;

import liraz.echo.domain.music.Artist;
import liraz.echo.repository.ArtistRepository;
import liraz.echo.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Transactional(readOnly = true)
    public List<Artist> search(String name) {
        if (name == null || name.isBlank()) {
            return artistRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        }
        return artistRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name.trim());
    }

    @Transactional(readOnly = true)
    public List<Artist> findAll() {
        return artistRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Artist require(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + id));
    }

    public Artist create(Artist artist) {
        return artistRepository.save(artist);
    }

    public Artist update(Long id, Artist data) {
        Artist artist = require(id);
        artist.setName(data.getName());
        artist.setCountry(data.getCountry());
        artist.setImageUrl(data.getImageUrl());
        return artistRepository.save(artist);
    }

    public void delete(Long id) {
        artistRepository.deleteById(id);
    }
}