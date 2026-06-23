package tinyswordsisland.controller;

import tinyswordsisland.model.IGameModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class SaveManager {

    private static final String APP_NAME = "TinySwordsIsland";
    private static final String PREFIX = "GameSaving_";
    private static final String EXT = ".dat";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private SaveManager() {}

    private static Path getBaseAppDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("mac")) {
            return Paths.get(userHome, "Documents",  APP_NAME);
        }

        if (os.contains("win")) {
            return Paths.get(userHome, "Documents",  APP_NAME);
        }

        return Paths.get(userHome, ".config", APP_NAME);
    }

    public static Path getSaveDir() throws IOException {
        Path saveDir = getBaseAppDir().resolve("saves");
        Files.createDirectories(saveDir);
        return saveDir;
    }

    public static Path saveGame(IGameModel model) throws IOException {
        Path saveDir = getSaveDir();

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String fileName = PREFIX + timestamp + EXT;
        Path savePath = saveDir.resolve(fileName);

        try (ObjectOutputStream out =
                     new ObjectOutputStream(Files.newOutputStream(savePath))) {
            out.writeObject(model);
        }

        return savePath;
    }

    public static IGameModel loadLatestGame() throws IOException, ClassNotFoundException {
        Path latest = getLatestSavePath();
        if (latest == null) {
            throw new FileNotFoundException("Nessun file di salvataggio trovato in " + getSaveDir().toAbsolutePath());
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(Files.newInputStream(latest))) {
            return (IGameModel) in.readObject();
        }
    }

    public static Path getLatestSavePath() throws IOException {
        Path saveDir = getSaveDir();

        try (Stream<Path> stream = Files.list(saveDir)) {
            Optional<Path> latest = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(PREFIX))
                    .filter(path -> path.getFileName().toString().endsWith(EXT))
                    .max(Comparator.comparingLong(path -> path.toFile().lastModified()));

            return latest.orElse(null);
        }
    }

    public static Path getSaveDirectoryPath() throws IOException {
        return getSaveDir().toAbsolutePath();
    }

}