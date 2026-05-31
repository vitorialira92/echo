package liraz.echo.domain.rating;

import java.io.Serializable;
import java.util.Objects;

public class SongRatingId implements Serializable {

    private Long userId;
    private Long songId;

    public SongRatingId() {
    }

    public SongRatingId(Long userId, Long songId) {
        this.userId = userId;
        this.songId = songId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SongRatingId that = (SongRatingId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(songId, that.songId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, songId);
    }

    @Override
    public String toString() {
        return "SongRatingId{userId=" + userId + ", songId=" + songId + "}";
    }
}
