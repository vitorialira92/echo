package liraz.echo;

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

import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class EchoApplication implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final SongRatingRepository songRatingRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final Scanner in = new Scanner(System.in);

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
        System.out.println();
        System.out.println("############################################");
        System.out.println("#              ECHO - Catalogo             #");
        System.out.println("############################################");
        System.out.println("Banco MySQL. Os dados de exemplo vao ser");
        System.out.println("carregados para que voce possa testar.");

        loadSampleData();

        boolean running = true;
        while (running) {
            printMainMenu();
            switch (prompt("Opcao")) {
                case "1" -> artistMenu();
                case "2" -> albumMenu();
                case "3" -> songMenu();
                case "4" -> userMenu();
                case "5" -> ratingMenu();
                case "0" -> running = false;
                default -> warn("Opcao invalida. Escolha um numero do menu.");
            }
        }
        System.out.println("\nEncerrando. Ate logo!");
    }

    private void printMainMenu() {
        banner("MENU PRINCIPAL");
        System.out.println("  1) Artistas");
        System.out.println("  2) Albuns");
        System.out.println("  3) Musicas");
        System.out.println("  4) Usuarios");
        System.out.println("  5) Avaliacoes");
        System.out.println("  0) Sair");
    }

    private void artistMenu() {
        boolean back = false;
        while (!back) {
            banner("ARTISTAS");
            System.out.println("  1) Listar todos");
            System.out.println("  2) Buscar por id");
            System.out.println("  3) Buscar por nome");
            System.out.println("  4) Criar");
            System.out.println("  5) Atualizar");
            System.out.println("  6) Remover");
            System.out.println("  0) Voltar");
            switch (prompt("Opcao")) {
                case "1" -> printList(artistRepository.findAll());
                case "2" -> findArtistById().ifPresent(System.out::println);
                case "3" -> printList(
                        artistRepository.findByNameContainingIgnoreCase(prompt("Nome (ou parte)")));
                case "4" -> createArtist();
                case "5" -> updateArtist();
                case "6" -> deleteArtist();
                case "0" -> back = true;
                default -> warn("Opcao invalida.");
            }
        }
    }

    private void albumMenu() {
        boolean back = false;
        while (!back) {
            banner("ALBUNS");
            System.out.println("  1) Listar todos");
            System.out.println("  2) Buscar por id");
            System.out.println("  3) Listar por artista");
            System.out.println("  4) Criar");
            System.out.println("  5) Atualizar");
            System.out.println("  6) Remover");
            System.out.println("  0) Voltar");
            switch (prompt("Opcao")) {
                case "1" -> printList(albumRepository.findAll());
                case "2" -> findAlbumById().ifPresent(System.out::println);
                case "3" -> printList(albumRepository.findByArtistId(promptLong("Id do artista")));
                case "4" -> createAlbum();
                case "5" -> updateAlbum();
                case "6" -> deleteAlbum();
                case "0" -> back = true;
                default -> warn("Opcao invalida.");
            }
        }
    }

    private void songMenu() {
        boolean back = false;
        while (!back) {
            banner("MUSICAS");
            System.out.println("  1) Listar todas");
            System.out.println("  2) Buscar por id");
            System.out.println("  3) Listar por album");
            System.out.println("  4) Listar por artista");
            System.out.println("  5) Criar");
            System.out.println("  6) Atualizar");
            System.out.println("  7) Remover");
            System.out.println("  0) Voltar");
            switch (prompt("Opcao")) {
                case "1" -> printList(songRepository.findAll());
                case "2" -> findSongById().ifPresent(System.out::println);
                case "3" -> printList(songRepository.findByAlbumId(promptLong("Id do album")));
                case "4" -> printList(songRepository.findByArtistId(promptLong("Id do artista")));
                case "5" -> createSong();
                case "6" -> updateSong();
                case "7" -> deleteSong();
                case "0" -> back = true;
                default -> warn("Opcao invalida.");
            }
        }
    }

    private void userMenu() {
        boolean back = false;
        while (!back) {
            banner("USUARIOS");
            System.out.println("  1) Listar todos");
            System.out.println("  2) Buscar por id");
            System.out.println("  3) Buscar por username");
            System.out.println("  4) Criar");
            System.out.println("  5) Atualizar");
            System.out.println("  6) Remover");
            System.out.println("  0) Voltar");
            switch (prompt("Opcao")) {
                case "1" -> printList(userRepository.findAll());
                case "2" -> findUserById().ifPresent(System.out::println);
                case "3" -> {
                    Optional<User> u = userRepository.findByUsername(prompt("Username"));
                    if (u.isEmpty()) {
                        warn("Nenhum usuario com esse username.");
                    } else {
                        System.out.println(u.get());
                    }
                }
                case "4" -> createUser();
                case "5" -> updateUser();
                case "6" -> deleteUser();
                case "0" -> back = true;
                default -> warn("Opcao invalida.");
            }
        }
    }

    private void ratingMenu() {
        boolean back = false;
        while (!back) {
            banner("AVALIACOES");
            System.out.println("  1) Listar por usuario");
            System.out.println("  2) Listar por musica");
            System.out.println("  3) Media de uma musica");
            System.out.println("  4) Criar / atualizar avaliacao");
            System.out.println("  5) Remover");
            System.out.println("  0) Voltar");
            switch (prompt("Opcao")) {
                case "1" -> printList(songRatingRepository.findByUserId(promptLong("Id do usuario")));
                case "2" -> printList(songRatingRepository.findBySongId(promptLong("Id da musica")));
                case "3" -> showAverage();
                case "4" -> upsertRating();
                case "5" -> deleteRating();
                case "0" -> back = true;
                default -> warn("Opcao invalida.");
            }
        }
    }


    // acesso ao bd
    private Optional<Artist> findArtistById() {
        Long id = promptLong("Id do artista");
        Optional<Artist> found = artistRepository.findById(id);
        if (found.isEmpty()) {
            warn("Nenhum artista com id " + id + ".");
        }
        return found;
    }

    private void createArtist() {
        String name = promptRequired("Nome");
        Country country = promptEnum("Pais", Country.class);
        String imageUrl = promptOptional("URL da imagem");
        save("Artista", () -> {
            Artist saved = artistRepository.save(new Artist(name, country, imageUrl));
            ok("Artista criado: " + saved);
        });
    }

    private void updateArtist() {
        Optional<Artist> found = findArtistById();
        if (found.isEmpty()) {
            return;
        }
        Artist a = found.get();
        a.setName(promptKeep("Nome", a.getName()));
        a.setCountry(promptEnumKeep("Pais", Country.class, a.getCountry()));
        a.setImageUrl(promptKeepOptional("URL da imagem", a.getImageUrl()));
        save("Artista", () -> {
            artistRepository.save(a);
            ok("Artista atualizado: " + a);
        });
    }

    private void deleteArtist() {
        Optional<Artist> found = findArtistById();
        if (found.isEmpty()) {
            return;
        }
        Long id = found.get().getId();
        if (!albumRepository.findByArtistId(id).isEmpty()) {
            warn("Esse artista possui albuns cadastrados. Remova-os antes.");
            return;
        }
        save("Artista", () -> {
            artistRepository.deleteById(id);
            ok("Artista removido.");
        });
    }

    private Optional<Album> findAlbumById() {
        Long id = promptLong("Id do album");
        Optional<Album> found = albumRepository.findById(id);
        if (found.isEmpty()) {
            warn("Nenhum album com id " + id + ".");
        }
        return found;
    }

    private void createAlbum() {
        Long artistId = promptLong("Id do artista");
        Optional<Artist> artistOpt = artistRepository.findById(artistId);
        if (artistOpt.isEmpty()) {
            warn("Nao existe artista com id " + artistId + ". Cadastre o artista primeiro.");
            return;
        }
        Artist artist = artistOpt.get();
        String title = promptRequired("Titulo");
        if (albumRepository.existsByArtistIdAndTitle(artistId, title)) {
            warn("Esse artista ja tem um album chamado \"" + title + "\".");
            return;
        }
        String cover = promptOptional("URL da capa");
        String spotify = promptOptional("URL do Spotify");
        Integer year = promptInt("Ano de lancamento");
        Genre genre = promptEnum("Genero", Genre.class);
        save("Album", () -> {
            Album saved = albumRepository.save(
                    new Album(artist, title, cover, spotify, year, genre));
            ok("Album criado: " + saved);
        });
    }

    private void updateAlbum() {
        Optional<Album> found = findAlbumById();
        if (found.isEmpty()) {
            return;
        }
        Album al = found.get();
        al.setTitle(promptKeep("Titulo", al.getTitle()));
        al.setCoverUrl(promptKeepOptional("URL da capa", al.getCoverUrl()));
        al.setSpotifyUrl(promptKeepOptional("URL do Spotify", al.getSpotifyUrl()));
        al.setReleaseYear(promptIntKeep("Ano de lancamento", al.getReleaseYear()));
        al.setGenre(promptEnumKeep("Genero", Genre.class, al.getGenre()));
        save("Album", () -> {
            albumRepository.save(al);
            ok("Album atualizado: " + al);
        });
    }

    private void deleteAlbum() {
        Optional<Album> found = findAlbumById();
        if (found.isEmpty()) {
            return;
        }
        Long id = found.get().getId();
        if (!songRepository.findByAlbumId(id).isEmpty()) {
            warn("Esse album possui musicas cadastradas. Remova-as antes.");
            return;
        }
        save("Album", () -> {
            albumRepository.deleteById(id);
            ok("Album removido.");
        });
    }

    private Optional<Song> findSongById() {
        Long id = promptLong("Id da musica");
        Optional<Song> found = songRepository.findById(id);
        if (found.isEmpty()) {
            warn("Nenhuma musica com id " + id + ".");
        }
        return found;
    }

    private void createSong() {
        Long albumId = promptLong("Id do album");
        Optional<Album> albumOpt = albumRepository.findById(albumId);
        if (albumOpt.isEmpty()) {
            warn("Nao existe album com id " + albumId + ". Cadastre o album primeiro.");
            return;
        }
        Album album = albumOpt.get();
        String title = promptRequired("Titulo");
        Integer track = promptInt("Numero da faixa");
        if (songRepository.existsByAlbumIdAndTrackNumber(albumId, track)) {
            warn("Ja existe a faixa numero " + track + " nesse album.");
            return;
        }
        Integer duration = promptInt("Duracao (segundos)");
        String lyrics = promptOptional("Letra");
        String spotify = promptOptional("URL do Spotify");
        Boolean explicit = promptBoolean("Conteudo explicito?");
        save("Musica", () -> {
            Song saved = songRepository.save(
                    new Song(album, title, track, duration, lyrics, spotify, explicit));
            ok("Musica criada: " + saved);
        });
    }

    private void updateSong() {
        Optional<Song> found = findSongById();
        if (found.isEmpty()) {
            return;
        }
        Song s = found.get();
        s.setTitle(promptKeep("Titulo", s.getTitle()));
        s.setTrackNumber(promptIntKeep("Numero da faixa", s.getTrackNumber()));
        s.setDurationSeconds(promptIntKeep("Duracao (segundos)", s.getDurationSeconds()));
        s.setLyrics(promptKeepOptional("Letra", s.getLyrics()));
        s.setSpotifyUrl(promptKeepOptional("URL do Spotify", s.getSpotifyUrl()));
        s.setExplicitContent(promptBooleanKeep("Conteudo explicito?", s.getExplicitContent()));
        save("Musica", () -> {
            songRepository.save(s);
            ok("Musica atualizada: " + s);
        });
    }

    private void deleteSong() {
        Optional<Song> found = findSongById();
        if (found.isEmpty()) {
            return;
        }
        Long id = found.get().getId();
        save("Musica", () -> {
            songRepository.deleteById(id);
            ok("Musica removida.");
        });
    }

    private Optional<User> findUserById() {
        Long id = promptLong("Id do usuario");
        Optional<User> found = userRepository.findById(id);
        if (found.isEmpty()) {
            warn("Nenhum usuario com id " + id + ".");
        }
        return found;
    }

    private void createUser() {
        String username = promptRequired("Username");
        if (userRepository.existsByUsername(username)) {
            warn("O username \"" + username + "\" ja esta em uso.");
            return;
        }
        String rawPassword = promptRequired("Senha");
        String name = promptRequired("Nome");
        Role role = promptEnum("Papel", Role.class);
        save("Usuario", () -> {
            User saved = userRepository.save(
                    new User(username, passwordEncoder.encode(rawPassword), name, role));
            ok("Usuario criado: " + saved);
        });
    }

    private void updateUser() {
        Optional<User> found = findUserById();
        if (found.isEmpty()) {
            return;
        }
        User u = found.get();
        u.setName(promptKeep("Nome", u.getName()));
        u.setRole(promptEnumKeep("Papel", Role.class, u.getRole()));
        String newPassword = promptOptional("Nova senha");
        if (newPassword != null) {
            u.setPassword(passwordEncoder.encode(newPassword));
        }
        save("Usuario", () -> {
            userRepository.save(u);
            ok("Usuario atualizado: " + u);
        });
    }

    private void deleteUser() {
        Optional<User> found = findUserById();
        if (found.isEmpty()) {
            return;
        }
        Long id = found.get().getId();
        if (!songRatingRepository.findByUserId(id).isEmpty()) {
            warn("Esse usuario possui avaliacoes. Remova-as antes.");
            return;
        }
        save("Usuario", () -> {
            userRepository.deleteById(id);
            ok("Usuario removido.");
        });
    }

    private void showAverage() {
        Long songId = promptLong("Id da musica");
        Double avg = songRatingRepository.averageRatingForSong(songId);
        if (avg == null) {
            System.out.println("Essa musica ainda nao tem avaliacoes.");
        } else {
            System.out.printf("Media da musica %d: %.2f%n", songId, avg);
        }
    }

    private void upsertRating() {
        Long userId = promptLong("Id do usuario");
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            warn("Nao existe usuario com id " + userId + ".");
            return;
        }
        Long songId = promptLong("Id da musica");
        Optional<Song> songOpt = songRepository.findById(songId);
        if (songOpt.isEmpty()) {
            warn("Nao existe musica com id " + songId + ".");
            return;
        }
        User user = userOpt.get();
        Song song = songOpt.get();
        Integer score = promptInt("Nota (1 a 10)");
        Feeling feeling = promptEnum("Sentimento", Feeling.class);
        String review = promptOptional("Resenha");

        Optional<SongRating> existing =
                songRatingRepository.findByUserIdAndSongId(userId, songId);
        if (existing.isPresent()) {
            SongRating r = existing.get();
            r.setRating(score);
            r.setFeeling(feeling);
            r.setReview(review);
            save("Avaliacao", () -> {
                songRatingRepository.save(r);
                ok("Avaliacao atualizada: " + r);
            });
        } else {
            save("Avaliacao", () -> {
                SongRating saved = songRatingRepository.save(
                        new SongRating(user, song, score, feeling, review));
                ok("Avaliacao criada: " + saved);
            });
        }
    }

    private void deleteRating() {
        Long userId = promptLong("Id do usuario");
        Long songId = promptLong("Id da musica");
        Optional<SongRating> existing =
                songRatingRepository.findByUserIdAndSongId(userId, songId);
        if (existing.isEmpty()) {
            warn("Nao ha avaliacao desse usuario para essa musica.");
            return;
        }
        save("Avaliacao", () -> {
            songRatingRepository.delete(existing.get());
            ok("Avaliacao removida.");
        });
    }


    // sample

    private void loadSampleData() {
        banner("DADOS DE EXEMPLO");
        if (userRepository.existsByUsername("admin")) {
            warn("Os dados de exemplo ja foram carregados.");
            return;
        }
        try {
            User admin = userRepository.save(new User(
                    "admin", passwordEncoder.encode("admin123"), "Administrador", Role.ADMIN));
            User luna = userRepository.save(new User(
                    "luna", passwordEncoder.encode("luna123"), "Luna Park", Role.USER));

            Artist newjeans = artistRepository.save(new Artist(
                    "NewJeans", Country.KR, "https://i.scdn.co/image/newjeans.jpg"));
            Album getUp = albumRepository.save(new Album(
                    newjeans, "Get Up",
                    "https://i.scdn.co/image/getup.jpg",
                    "https://open.spotify.com/album/3vWA9PhuFr3JKjFkfJTzvY",
                    2023, Genre.KPOP));

            Song s1 = songRepository.save(new Song(getUp, "New Jeans", 1, 110, null,
                    "https://open.spotify.com/track/3eGqHquUTbsynPEzlt5RJv", false));
            Song s2 = songRepository.save(new Song(getUp, "Super Shy", 2, 154,
                    "Cause I'm super shy, super shy...",
                    "https://open.spotify.com/track/5sYsTRWHksKZv2zVuKQ8ME", false));
            songRepository.save(new Song(getUp, "ETA", 3, 152, null, null, false));

            songRatingRepository.save(new SongRating(
                    luna, s2, 9, Feeling.EXCITED, "Grudenta e viciante!"));

            ok("Dados de exemplo carregados:");
            System.out.println("  - usuarios: admin (id " + admin.getId()
                    + "), luna (id " + luna.getId() + ")");
            System.out.println("  - artista NewJeans (id " + newjeans.getId() + ")");
            System.out.println("  - album Get Up (id " + getUp.getId() + ") com 3 musicas");
            System.out.println("  - 1 avaliacao da luna para \"Super Shy\" (id " + s2.getId() + ")");
        } catch (RuntimeException e) {
            reportError(e);
        }
    }

    //helpers
    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private String promptRequired(String label) {
        while (true) {
            String value = prompt(label);
            if (!value.isEmpty()) {
                return value;
            }
            warn("Esse campo e obrigatorio.");
        }
    }

    private String promptOptional(String label) {
        System.out.print(label + " (opcional, Enter para pular): ");
        String value = in.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    private Integer promptInt(String label) {
        while (true) {
            try {
                return Integer.parseInt(prompt(label));
            } catch (NumberFormatException e) {
                warn("Digite um numero inteiro valido.");
            }
        }
    }

    private Long promptLong(String label) {
        while (true) {
            try {
                return Long.parseLong(prompt(label));
            } catch (NumberFormatException e) {
                warn("Digite um numero inteiro valido.");
            }
        }
    }

    private Boolean promptBoolean(String label) {
        while (true) {
            String value = prompt(label + " (s/n)").toLowerCase();
            if (value.equals("s") || value.equals("sim")) {
                return true;
            }
            if (value.equals("n") || value.equals("nao")) {
                return false;
            }
            warn("Responda com 's' ou 'n'.");
        }
    }

    private <E extends Enum<E>> E promptEnum(String label, Class<E> type) {
        System.out.println(label + " - opcoes: " + optionsOf(type));
        while (true) {
            String value = prompt("  valor").toUpperCase();
            try {
                return Enum.valueOf(type, value);
            } catch (IllegalArgumentException e) {
                warn("Opcao invalida. Escolha uma das listadas acima.");
            }
        }
    }

    private String promptKeep(String label, String current) {
        System.out.print(label + " [" + current + "]: ");
        String value = in.nextLine().trim();
        return value.isEmpty() ? current : value;
    }

    private String promptKeepOptional(String label, String current) {
        System.out.print(label + " [" + (current == null ? "vazio" : current)
                + "] (Enter mantem): ");
        String value = in.nextLine().trim();
        return value.isEmpty() ? current : value;
    }

    private Integer promptIntKeep(String label, Integer current) {
        while (true) {
            System.out.print(label + " [" + current + "]: ");
            String value = in.nextLine().trim();
            if (value.isEmpty()) {
                return current;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                warn("Digite um numero inteiro valido.");
            }
        }
    }

    private Boolean promptBooleanKeep(String label, Boolean current) {
        System.out.print(label + " (s/n) [" + (Boolean.TRUE.equals(current) ? "s" : "n")
                + "]: ");
        String value = in.nextLine().trim().toLowerCase();
        if (value.isEmpty()) {
            return current;
        }
        return value.equals("s") || value.equals("sim");
    }

    private <E extends Enum<E>> E promptEnumKeep(String label, Class<E> type, E current) {
        System.out.println(label + " - opcoes: " + optionsOf(type));
        System.out.print("  valor [" + current + "] (Enter mantem): ");
        String value = in.nextLine().trim().toUpperCase();
        if (value.isEmpty()) {
            return current;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            warn("Opcao invalida; mantendo " + current + ".");
            return current;
        }
    }

    private static <E extends Enum<E>> String optionsOf(Class<E> type) {
        StringBuilder sb = new StringBuilder();
        for (E constant : type.getEnumConstants()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(constant.name());
        }
        return sb.toString();
    }

    private void save(String entity, Runnable action) {
        try {
            action.run();
        } catch (ConstraintViolationException e) {
            warn(entity + " nao salvo - dados invalidos: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            warn(entity + " nao salvo - viola uma restricao do banco "
                    + "(valor duplicado ou obrigatorio ausente).");
        } catch (RuntimeException e) {
            reportError(e);
        }
    }

    private void reportError(RuntimeException e) {
        warn("Ocorreu um erro inesperado: " + rootMessage(e));
    }

    private static String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

    private static void printList(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("(nenhum registro)");
            return;
        }
        items.forEach(item -> System.out.println("  - " + item));
    }

    private static void banner(String text) {
        System.out.println();
        System.out.println("==================== " + text + " ====================");
    }

    private static void ok(String text) {
        System.out.println("[ok] " + text);
    }

    private static void warn(String text) {
        System.out.println("[!] " + text);
    }
}
