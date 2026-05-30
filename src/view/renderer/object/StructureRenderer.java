package view.renderer.object;

import model.object.GameObject;
import view.SpriteLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderer for static structures (no animation).
 */
public class StructureRenderer extends ObjectRender<GameObject> {

    private final Map<String, BufferedImage> spriteByPath = new HashMap<>();

    @Override
    public void update(GameObject obj, double deltaMs) {
        // Static object: no animation/update needed.
    }

    @Override
    public void draw(Graphics2D g2, GameObject obj, int screenX, int screenY) {
        BufferedImage sprite = spriteByPath.computeIfAbsent("res/object/buildings/Castle_Blue.png", SpriteLoader::loadSpriteSheet);
        g2.drawImage(sprite, screenX, screenY, obj.getWidth(), obj.getHeight(), null);
    }
}
