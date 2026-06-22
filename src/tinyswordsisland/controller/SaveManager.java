package tinyswordsisland.controller;

import tinyswordsisland.model.IGameModel;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class SaveManager {

    private static final Path SAVE_DIR = Paths.get("saves");
    private static final String PREFIX = "GameSaving_";
    private static final String EXT = ".dat";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private SaveManager() {}

    public static Path saveGame(IGameModel model) throws IOException {
        Files.createDirectories(SAVE_DIR);

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String fileName = PREFIX + timestamp + EXT;
        Path savePath = SAVE_DIR.resolve(fileName);

        try (ObjectOutputStream out =
                     new ObjectOutputStream(Files.newOutputStream(savePath))) {
            out.writeObject(model);
        }

        return savePath;
    }

    public static IGameModel loadLatestGame() throws IOException, ClassNotFoundException {
        Path latest = getLatestSavePath();
        if (latest == null) {
            throw new FileNotFoundException("Nessun file di salvataggio trovato in " + SAVE_DIR.toAbsolutePath());
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(Files.newInputStream(latest))) {
            return (IGameModel) in.readObject();
        }
    }

    public static Path getLatestSavePath() throws IOException {
        if (!Files.exists(SAVE_DIR) || !Files.isDirectory(SAVE_DIR)) {
            return null;
        }

        try (Stream<Path> stream = Files.list(SAVE_DIR)) {
            Optional<Path> latest = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(PREFIX))
                    .filter(path -> path.getFileName().toString().endsWith(EXT))
                    .max(Comparator.comparingLong(path -> path.toFile().lastModified()));

            return latest.orElse(null);
        }
    }
}