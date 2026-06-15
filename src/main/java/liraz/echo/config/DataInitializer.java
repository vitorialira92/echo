package liraz.echo.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import liraz.echo.domain.music.Album;
import liraz.echo.domain.music.Artist;
import liraz.echo.domain.music.Country;
import liraz.echo.domain.music.Genre;
import liraz.echo.domain.music.Song;
import liraz.echo.domain.rating.Feeling;
import liraz.echo.domain.rating.SongRating;
import liraz.echo.domain.user.Role;
import liraz.echo.domain.user.User;
import liraz.echo.repository.AlbumRepository;
import liraz.echo.repository.ArtistRepository;
import liraz.echo.repository.SongRatingRepository;
import liraz.echo.repository.SongRepository;
import liraz.echo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String MUSIC_SEED = "seed/music_seed.json";
    private static final String USERS_SEED = "seed/users_seed.json";
    private static final String RATINGS_SEED = "seed/ratings_seed.json";

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final SongRatingRepository songRatingRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataInitializer(UserRepository userRepository,
                           ArtistRepository artistRepository,
                           AlbumRepository albumRepository,
                           SongRepository songRepository,
                           SongRatingRepository songRatingRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.songRatingRepository = songRatingRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedMusic();
        seedRatings();
    }

    private void seedUsers() throws Exception {
        if (userRepository.count() > 0) {
            return;
        }
        List<UserSeed> users = readList(USERS_SEED, UserSeed[].class);
        int created = 0;
        for (UserSeed seed : users) {
            if (seed.username() == null || userRepository.existsByUsername(seed.username())) {
                continue;
            }
            userRepository.save(new User(
                    seed.username(),
                    passwordEncoder.encode(seed.password()),
                    seed.name(),
                    seed.role()));
            created++;
        }
        log.info("Seed: {} users created.", created);
    }

    private void seedMusic() throws Exception {
        if (artistRepository.count() > 0) {
            return;
        }
        MusicSeed music = readObject(MUSIC_SEED, MusicSeed.class);
        if (music == null || music.artists() == null) {
            return;
        }
        int artists = 0;
        int albums = 0;
        int songs = 0;
        for (ArtistSeed artistSeed : music.artists()) {
            if (artistSeed.name() == null || artistSeed.name().isBlank()) {
                continue;
            }
            Artist artist = artistRepository.save(new Artist(
                    artistSeed.name(),
                    artistSeed.country(),
                    artistSeed.imageUrl()));
            artists++;
            if (artistSeed.albums() == null) {
                continue;
            }
            for (AlbumSeed albumSeed : artistSeed.albums()) {
                if (albumSeed.name() == null || albumSeed.name().isBlank()) {
                    continue;
                }
                Genre genre = albumSeed.genre() != null ? albumSeed.genre() : Genre.POP;
                Integer year = albumSeed.year() != null ? albumSeed.year() : 0;
                Album album = albumRepository.save(new Album(
                        artist,
                        albumSeed.name(),
                        albumSeed.coverUrl(),
                        albumSeed.spotifyUrl(),
                        year,
                        genre));
                albums++;
                if (albumSeed.songs() == null) {
                    continue;
                }
                Set<Integer> usedTracks = new HashSet<>();
                int fallbackTrack = 0;
                for (SongSeed songSeed : albumSeed.songs()) {
                    if (songSeed.title() == null || songSeed.title().isBlank()) {
                        continue;
                    }
                    Integer trackNumber = songSeed.trackNumber();
                    if (trackNumber == null || trackNumber < 1 || usedTracks.contains(trackNumber)) {
                        do {
                            fallbackTrack++;
                        } while (usedTracks.contains(fallbackTrack));
                        trackNumber = fallbackTrack;
                    }
                    usedTracks.add(trackNumber);
                    Integer duration = (songSeed.durationSeconds() != null && songSeed.durationSeconds() >= 1)
                            ? songSeed.durationSeconds()
                            : 1;
                    songRepository.save(new Song(
                            album,
                            songSeed.title(),
                            trackNumber,
                            duration,
                            songSeed.lyrics(),
                            songSeed.spotifyUrl(),
                            songSeed.explicit() != null && songSeed.explicit()));
                    songs++;
                }
            }
        }
        log.info("Seed: {} artists, {} albuns and {} songs created.", artists, albums, songs);
    }

    private void seedRatings() throws Exception {
        if (songRatingRepository.count() > 0) {
            return;
        }
        List<RatingSeed> ratings = readList(RATINGS_SEED, RatingSeed[].class);
        int created = 0;
        for (RatingSeed seed : ratings) {
            User user = seed.username() == null
                    ? null
                    : userRepository.findByUsername(seed.username()).orElse(null);
            if (user == null) {
                log.warn("Seed ratings: User with username '{}' not found, skipping it.", seed.username());
                continue;
            }
            Song song = findSong(seed.artist(), seed.album(), seed.song());
            if (song == null) {
                log.warn("Seed ratings: Song '{} / {} / {}' nnot found, skipping it.",
                        seed.artist(), seed.album(), seed.song());
                continue;
            }
            if (songRatingRepository.findByUserIdAndSongId(user.getId(), song.getId()).isPresent()) {
                continue;
            }
            songRatingRepository.save(new SongRating(
                    user, song, seed.rating(), seed.feeling(), seed.review()));
            created++;
        }
        log.info("Seed: {} ratings created.", created);
    }

    private Song findSong(String artistName, String albumTitle, String songTitle) {
        if (artistName == null || albumTitle == null || songTitle == null) {
            return null;
        }
        Artist artist = artistRepository.findByNameContainingIgnoreCase(artistName).stream()
                .filter(a -> a.getName().equalsIgnoreCase(artistName))
                .findFirst()
                .orElse(null);
        if (artist == null) {
            return null;
        }
        Album album = albumRepository.findByArtistId(artist.getId()).stream()
                .filter(a -> a.getTitle().equalsIgnoreCase(albumTitle))
                .findFirst()
                .orElse(null);
        if (album == null) {
            return null;
        }
        return songRepository.findByAlbumId(album.getId()).stream()
                .filter(s -> s.getTitle().equalsIgnoreCase(songTitle))
                .findFirst()
                .orElse(null);
    }

    private <T> T readObject(String path, Class<T> type) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("Seed: file '{}' not found in the expected classpath, skipping it.", path);
            return null;
        }
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }

    private <T> List<T> readList(String path, Class<T[]> arrayType) throws Exception {
        T[] array = readObject(path, arrayType);
        return array == null ? List.of() : Arrays.asList(array);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MusicSeed(List<ArtistSeed> artists) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ArtistSeed(String name, Country country, String imageUrl, List<AlbumSeed> albums) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AlbumSeed(String name, Integer year, Genre genre, String coverUrl, String spotifyUrl, List<SongSeed> songs) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SongSeed(String title, Integer trackNumber, Integer durationSeconds, String lyrics, String spotifyUrl, Boolean explicit) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UserSeed(String username, String password, String name, Role role) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RatingSeed(String username, String artist, String album, String song, Integer rating, Feeling feeling, String review) { }
}