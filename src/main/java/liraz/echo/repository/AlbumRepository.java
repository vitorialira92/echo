package liraz.echo.repository;

import liraz.echo.domain.music.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByArtistId(Long artistId);
    List<Album> findByArtistIdOrderByReleaseYearDescTitleAsc(Long artistId);
    boolean existsByArtistIdAndTitle(Long artistId, String title);
    boolean existsByArtistIdAndTitleAndIdNot(Long artistId, String title, Long id);
}
