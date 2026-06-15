package liraz.echo.service;

import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Song;
import liraz.echo.exceptions.ResourceNotFoundException;
import liraz.echo.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SongService {

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Transactional(readOnly = true)
    public List<Song> findByAlbum(Long albumId) {
        return songRepository.findByAlbumId(albumId);
    }

    @Transactional(readOnly = true)
    public Song require(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean trackTakenForAlbum(Long albumId, Integer trackNumber) {
        return songRepository.existsByAlbumIdAndTrackNumber(albumId, trackNumber);
    }

    @Transactional(readOnly = true)
    public boolean trackTakenByOther(Long albumId, Integer trackNumber, Long songId) {
        return songRepository.existsByAlbumIdAndTrackNumberAndIdNot(albumId, trackNumber, songId);
    }

    public Song create(Album album, Song data) {
        data.setAlbum(album);
        return songRepository.save(data);
    }

    public Song update(Long id, Song data) {
        Song song = require(id);
        song.setTitle(data.getTitle());
        song.setTrackNumber(data.getTrackNumber());
        song.setDurationSeconds(data.getDurationSeconds());
        song.setLyrics(data.getLyrics());
        song.setSpotifyUrl(data.getSpotifyUrl());
        song.setExplicitContent(data.getExplicitContent());
        return songRepository.save(song);
    }

    public void delete(Long id) {
        songRepository.deleteById(id);
    }
}
