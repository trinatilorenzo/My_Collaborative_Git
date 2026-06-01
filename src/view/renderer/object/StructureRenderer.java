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
        String spritePath = getSpritePath(obj.getName());
        BufferedImage sprite = spriteByPath.computeIfAbsent(spritePath, SpriteLoader::loadSpriteSheet);
        g2.drawImage(sprite, screenX, screenY, obj.getWidth(), obj.getHeight(), null);
    }

    private String getSpritePath(String objectName) {
        if (objectName == null) {
            return "/res/object/buildings/Castle_Blue.png";
        }

        return switch (objectName.trim().toLowerCase()) {
            case "castle" -> "/res/object/buildings/Castle_Blue.png";
            case "tower" -> "/res/object/buildings/Tower_Blue.png";
            case "goldmine" -> "/res/object/buildings/GoldMine_Active.png";
            case "goblin_home", "goblin_house" -> "/res/object/buildings/Goblin_House.png";
            default -> "/res/object/buildings/Castle_Blue.png";
        };
    }
}
