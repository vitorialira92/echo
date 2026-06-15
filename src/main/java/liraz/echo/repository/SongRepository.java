package liraz.echo.repository;

import liraz.echo.domain.music.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByAlbumId(Long albumId);
    boolean existsByAlbumIdAndTrackNumber(Long albumId, Integer trackNumber);
    boolean existsByAlbumIdAndTrackNumberAndIdNot(Long albumId, Integer trackNumber, Long id);


    @Query("select s from Song s where s.album.artist.id = :artistId")
    List<Song> findByArtistId(@Param("artistId") Long artistId);
}
