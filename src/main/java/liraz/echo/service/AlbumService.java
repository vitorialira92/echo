package liraz.echo.service;

import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Artist;
import liraz.echo.repository.AlbumRepository;
import liraz.echo.repository.SongRepository;
import liraz.echo.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;

    public AlbumService(AlbumRepository albumRepository, SongRepository songRepository) {
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
    }

    @Transactional(readOnly = true)
    public List<Album> findByArtist(Long artistId) {
        return albumRepository.findByArtistIdOrderByReleaseYearDescTitleAsc(artistId);
    }

    @Transactional(readOnly = true)
    public Album require(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean titleTakenForArtist(Long artistId, String title) {
        return albumRepository.existsByArtistIdAndTitle(artistId, title);
    }

    @Transactional(readOnly = true)
    public boolean titleTakenByOther(Long artistId, String title, Long albumId) {
        return albumRepository.existsByArtistIdAndTitleAndIdNot(artistId, title, albumId);
    }

    @Transactional(readOnly = true)
    public boolean hasAlbums(Long artistId) {
        return !albumRepository.findByArtistId(artistId).isEmpty();
    }

    public Album create(Artist artist, Album data) {
        data.setArtist(artist);
        return albumRepository.save(data);
    }

    public Album update(Long id, Album data) {
        Album album = require(id);
        album.setTitle(data.getTitle());
        album.setCoverUrl(data.getCoverUrl());
        album.setSpotifyUrl(data.getSpotifyUrl());
        album.setGenre(data.getGenre());
        album.setReleaseYear(data.getReleaseYear());
        return albumRepository.save(album);
    }

    public void delete(Long id) {
        albumRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean hasSongs(Long albumId) {
        return !songRepository.findByAlbumId(albumId).isEmpty();
    }
}