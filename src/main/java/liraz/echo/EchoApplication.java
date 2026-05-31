package liraz.echo;

import liraz.echo.domain.music.*;
import liraz.echo.domain.rating.Feeling;
import liraz.echo.domain.rating.SongRating;
import liraz.echo.domain.rating.SongRatingId;
import liraz.echo.domain.user.Role;
import liraz.echo.domain.user.User;
import liraz.echo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EchoApplication implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final SongRatingRepository songRatingRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public EchoApplication(UserRepository userRepository,
                           ArtistRepository artistRepository,
                           AlbumRepository albumRepository,
                           SongRepository songRepository,
                           SongRatingRepository songRatingRepository) {
        this.userRepository = userRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.songRatingRepository = songRatingRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(EchoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        banner("ECHO - T5 JPA CRUD DEMO");

        banner("CREATE");

        User admin = userRepository.save(
                new User("admin", passwordEncoder.encode("admin123"), "System Administrator", Role.ADMIN));
        User luna = userRepository.save(
                new User("luna", passwordEncoder.encode("luna123"), "Luna Park", Role.USER));
        System.out.println("Created users:");
        System.out.println("  " + admin + "  passwordHash=" + shorten(admin.getPassword()));
        System.out.println("  " + luna + "  passwordHash=" + shorten(luna.getPassword()));

        Artist newjeans = artistRepository.save(
                new Artist("NewJeans", Country.KR, "https://i.scdn.co/image/newjeans.jpg"));
        System.out.println("Created artist: " + newjeans);

        Album getUp = albumRepository.save(new Album(
                newjeans.getId(), "Get Up",
                "https://i.scdn.co/image/getup.jpg",
                "https://open.spotify.com/album/3vWA9PhuFr3JKjFkfJTzvY",
                2023, Genre.KPOP));
        System.out.println("Created album: " + getUp);

        Song s1 = songRepository.save(new Song(
                getUp.getId(), "New Jeans", 1, 110, null,
                "https://open.spotify.com/track/3eGqHquUTbsynPEzlt5RJv", false));
        Song s2 = songRepository.save(new Song(
                getUp.getId(), "Super Shy", 2, 154, "Cause I, I, I'm super shy, super shy...",
                "https://open.spotify.com/track/5sYsTRWHksKZv2zVuKQ8ME", false));
        Song s3 = songRepository.save(new Song(
                getUp.getId(), "ETA", 3, 152, null, null, false));
        System.out.println("Created songs:");
        System.out.println("  " + s1);
        System.out.println("  " + s2);
        System.out.println("  " + s3);

        SongRating rating = songRatingRepository.save(
                new SongRating(luna.getId(), s2.getId(), 9, Feeling.EXCITED, "Catchy and addictive!"));
        System.out.println("Created rating: " + rating);

        banner("READ");
        System.out.println("All users (" + userRepository.count() + "): " + userRepository.findAll());
        System.out.println("Artist by id " + newjeans.getId() + ": "
                + artistRepository.findById(newjeans.getId()).orElseThrow());
        System.out.println("Albums of artist " + newjeans.getId() + ": "
                + albumRepository.findByArtistId(newjeans.getId()));
        System.out.println("Songs of album " + getUp.getId() + ": "
                + songRepository.findByAlbumId(getUp.getId()));
        System.out.println("Songs of artist " + newjeans.getId() + " (JPQL join): "
                + songRepository.findByArtistId(newjeans.getId()));
        System.out.println("Ratings by user " + luna.getId() + ": "
                + songRatingRepository.findByUserId(luna.getId()));
        System.out.println("Average rating of song " + s2.getId() + ": "
                + songRatingRepository.averageRatingForSong(s2.getId()));
        System.out.println("Find user by username 'admin': "
                + userRepository.findByUsername("admin").orElseThrow());
        System.out.println("Uniqueness checks (used to reject duplicates):");
        System.out.println("  username 'luna' already exists?        " + userRepository.existsByUsername("luna"));
        System.out.println("  album 'Get Up' for this artist exists? "
                + albumRepository.existsByArtistIdAndTitle(newjeans.getId(), "Get Up"));
        System.out.println("  track #2 in this album exists?         "
                + songRepository.existsByAlbumIdAndTrackNumber(getUp.getId(), 2));

        banner("UPDATE");
        Song songToUpdate = songRepository.findById(s3.getId()).orElseThrow();
        songToUpdate.setTitle("ETA (Remix)");
        songRepository.save(songToUpdate);
        System.out.println("Updated song title: " + songRepository.findById(s3.getId()).orElseThrow());

        SongRating ratingToUpdate = songRatingRepository
                .findByUserIdAndSongId(luna.getId(), s2.getId()).orElseThrow();
        ratingToUpdate.setRating(10);
        ratingToUpdate.setFeeling(Feeling.HAPPY);
        ratingToUpdate.setReview("Even better on repeat!");
        songRatingRepository.save(ratingToUpdate);
        System.out.println("Updated rating (createdAt preserved): "
                + songRatingRepository.findByUserIdAndSongId(luna.getId(), s2.getId()).orElseThrow());

        User userToUpdate = userRepository.findById(luna.getId()).orElseThrow();
        userToUpdate.setName("Luna J. Park");
        userRepository.save(userToUpdate);
        System.out.println("Updated user name: " + userRepository.findById(luna.getId()).orElseThrow());

        banner("DELETE");
        songRatingRepository.deleteById(new SongRatingId(luna.getId(), s2.getId()));
        System.out.println("Deleted rating. Remaining ratings of song " + s2.getId() + ": "
                + songRatingRepository.findBySongId(s2.getId()).size());

        songRepository.deleteById(s1.getId());
        System.out.println("Deleted song #1. Remaining songs of album: "
                + songRepository.findByAlbumId(getUp.getId()));

        banner("FINAL COUNTS");
        System.out.println("users=" + userRepository.count()
                + ", artists=" + artistRepository.count()
                + ", albums=" + albumRepository.count()
                + ", songs=" + songRepository.count()
                + ", ratings=" + songRatingRepository.count());

        banner("DEMO COMPLETE");
    }

    private static void banner(String text) {
        String line = "=".repeat(54);
        System.out.println();
        System.out.println(line);
        System.out.println("  " + text);
        System.out.println(line);
    }

    private static String shorten(String value) {
        if (value == null) {
            return "null";
        }
        return value.length() <= 24 ? value : value.substring(0, 24) + "...";
    }
}

