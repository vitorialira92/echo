package liraz.echo.dto.catalog;

import liraz.echo.domain.music.Song;

public record SongResponse(
        Long id,
        Long albumId,
        String title,
        Integer trackNumber,
        Integer durationSeconds,
        String lyrics,
        String spotifyUrl,
        Boolean explicitContent
) {
    public static SongResponse from(Song song) {
        return new SongResponse(
                song.getId(),
                song.getAlbum().getId(),
                song.getTitle(),
                song.getTrackNumber(),
                song.getDurationSeconds(),
                song.getLyrics(),
                song.getSpotifyUrl(),
                song.getExplicitContent()
        );
    }
}
