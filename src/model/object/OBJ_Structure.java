package model.object;

import java.awt.Rectangle;

/**
 * Static structure loaded from TMX object layers.
 * It has no animation and no interaction behavior.
 */
public class OBJ_Structure extends GameObject {

    private final String spritePath;

    public OBJ_Structure(int worldX, int worldY, int width, int height, String spritePath) {
        // Pass essential data up to the GameObject base constructor
        super("structure", worldX, worldY, width, height, new Rectangle(0, 0, width, height));
        
        this.spritePath = spritePath;
        this.solid = true;
    }

    @Override
    public void update(double deltaMs) {
        // Static objects do not require runtime logic updates
    }

    public String getSpritePath() {
        return spritePath;
    }
}