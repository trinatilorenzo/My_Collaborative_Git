package model;

import model.GameModel;

import java.io.*;

public final class SaveManager {

    private SaveManager() {}

    public static void saveGame(GameModel model, String filePath) throws IOException {
        model.beforeSave();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(model);
        }
    }

    public static GameModel loadGame(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            GameModel loaded = (GameModel) in.readObject();
            loaded.afterLoad();
            return loaded;
        }
    }

    public static boolean saveExists(String path) {
        return new File(path).exists();
    }
}