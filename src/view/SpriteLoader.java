package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpriteLoader {
    // A simple sprite loader that loads and caches spritesheets to avoid redundant loading
    private static final Map<String, BufferedImage> spriteSheets = new HashMap<>();

    public static BufferedImage loadSpriteSheet(String path){
        if (spriteSheets.containsKey(path)){
            return spriteSheets.get(path);
        }
        try {
            BufferedImage spriteSheet = ImageIO.read(SpriteLoader.class.getResourceAsStream(path));
            spriteSheets.put(path, spriteSheet);
            return spriteSheet;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sprite sheet: " + path, e);
        }
    }

    public static BufferedImage[] getAnimationFrames(BufferedImage sheet, int startRow, int rows, int cols, int spriteWidth, int spriteHeight){
        BufferedImage[] frames = new BufferedImage[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                frames[index++] = sheet.getSubimage(
                    j * spriteWidth,
                    (startRow + i) * spriteHeight,
                    spriteWidth,
                    spriteHeight
                );
            }
        }
        return frames;
    }
}
