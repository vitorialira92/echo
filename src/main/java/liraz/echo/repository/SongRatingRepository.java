package liraz.echo.repository;

import liraz.echo.domain.rating.SongRating;
import liraz.echo.domain.rating.SongRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRatingRepository extends JpaRepository<SongRating, SongRatingId> {
    List<SongRating> findByUserId(Long userId);
    List<SongRating> findBySongId(Long songId);
    Optional<SongRating> findByUserIdAndSongId(Long userId, Long songId);

    @Query("select avg(r.rating) from SongRating r where r.songId = :songId")
    Double averageRatingForSong(@Param("songId") Long songId);
}
