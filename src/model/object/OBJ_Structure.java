package model.object;

import java.awt.Rectangle;

/**
 * Static structure loaded from TMX object layers.
 * It has no animation and no interaction behavior.
 */
public class OBJ_Structure extends GameObject {

    private final String spritePath;

    public OBJ_Structure(int worldX, int worldY, int width, int height, String spritePath) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
        this.spritePath = spritePath;
        this.name = "structure";

        // Requested: solid area equals image/rendered size.
        this.solidArea = new Rectangle(0, 0, width, height);
        this.solid = true;
    }

    public String getSpritePath() {
        return spritePath;
    }
}
