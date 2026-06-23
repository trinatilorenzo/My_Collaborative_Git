package tinyswordsisland.view.renderer;

import tinyswordsisland.model.IRenderable;
import tinyswordsisland.model.RenderableType;
import tinyswordsisland.view.ViewEvent;
import tinyswordsisland.view.renderer.entity.*;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.model.enu.PlayerColor;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import tinyswordsisland.controller.IController;

public class RenderDispatcher {
    private final PlayerRender playerRender;
    private final MonkRenderer monkRenderer;
    private final TNTRenderer tntRenderer;
    private final DynamiteRender dynamiteRender;
    private final TorchRenderer torchRenderer;
    private final GameObjectRenderer objectRenderer;

    public RenderDispatcher(GameConfig GS, PlayerColor initialPlayerColor) {
        this.playerRender = new PlayerRender(GS.entityConfig(), initialPlayerColor);
        this.monkRenderer = new MonkRenderer(GS.entityConfig());
        this.tntRenderer = new TNTRenderer(GS.entityConfig());
        this.dynamiteRender = new DynamiteRender(GS.entityConfig());
        this.torchRenderer = new TorchRenderer(GS.entityConfig());
        this.objectRenderer = new GameObjectRenderer();
    }

    /**
     * Manages the drawing of the entity and, if debugMode is active, automatically draws the Solid Area
     */
    public void draw(Graphics2D g2, IRenderable obj, int screenX, int screenY, boolean debugMode, int playerWorldX, int playerWorldY, int playerCurrentLayer) {
        RenderableType type = obj.getRenderableType();
        if (type == RenderableType.PLAYER) {
            playerRender.draw(g2, obj, screenX, screenY);
            if (debugMode) {
                playerRender.drawSolidArea(g2, obj, screenX, screenY);
            }
        }
        else if (type == RenderableType.MONK) {
            monkRenderer.draw(g2, obj, screenX, screenY);
            if (debugMode) {
                monkRenderer.drawSolidArea(g2, obj, screenX, screenY);
            }
        }
        else if (type == RenderableType.ENEMY_TNT) {
            tntRenderer.draw(g2, obj, screenX, screenY);
            if (debugMode) {
                tntRenderer.drawSolidArea(g2, obj, screenX, screenY);
            }
        }
        else if (type == RenderableType.ENEMY_DYNAMITE) {
            dynamiteRender.draw(g2, obj, screenX, screenY);
            if (debugMode) {
                dynamiteRender.drawSolidArea(g2, obj, screenX, screenY);
            }
        }
        else if (type == RenderableType.ENEMY_TORCH) {
            torchRenderer.draw(g2, obj, screenX, screenY);
            if (debugMode) {
                torchRenderer.drawSolidArea(g2, obj, screenX, screenY);
            }
        }
        else if (type == RenderableType.DYNAMITE_PROJECTILE) {
            dynamiteRender.drawProjectile(g2, obj, screenX, screenY);
            if (debugMode) {
                dynamiteRender.drawProjectileSolidArea(g2, obj, screenX, screenY);
            }
        }
        else if (type == RenderableType.GAME_OBJECT) {
            objectRenderer.draw(g2, obj, screenX, screenY);
            if (debugMode) {
                objectRenderer.drawDebugObject(g2, obj, screenX, screenY, playerCurrentLayer);
            }
        }
    }

    /**
     * Updates the animations of all entities and game objects in the tinyswordsisland.model.
     */
    public List<ViewEvent> update(IController controller, double deltaMs) {
        List<ViewEvent> events = new ArrayList<>();
        for (IRenderable obj : controller.getAllRenderables()) {
            RenderableType type = obj.getRenderableType();
            if (type == RenderableType.PLAYER) {
                playerRender.update(obj, deltaMs);
                events.addAll(playerRender.consumeViewEvents());
            }
            else if (type == RenderableType.MONK) monkRenderer.update(obj, deltaMs);
            else if (type == RenderableType.ENEMY_TNT) tntRenderer.update(obj, deltaMs);
            else if (type == RenderableType.ENEMY_DYNAMITE) dynamiteRender.update(obj, deltaMs);
            else if (type == RenderableType.ENEMY_TORCH) torchRenderer.update(obj, deltaMs);
            else if (type == RenderableType.GAME_OBJECT) objectRenderer.update(obj, deltaMs);
        }
        return events;
    }

    public void updatePlayerColor(PlayerColor color) {
        playerRender.setPlayerColor(color);
    }
}