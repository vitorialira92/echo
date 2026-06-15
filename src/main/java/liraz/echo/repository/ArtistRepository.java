package liraz.echo.repository;

import liraz.echo.domain.music.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByNameContainingIgnoreCase(String name);
    List<Artist> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
