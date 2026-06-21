package view.renderer;

import model.IRenderable;
import view.renderer.entity.*;
import main.CONFIG.GameConfig;
import main.CONFIG.enu.PlayerColor;

import java.awt.Graphics2D;

import controller.IController;

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
        if (obj instanceof model.entity.Player p) {
            playerRender.draw(g2, p, screenX, screenY);
            if (debugMode) {
                playerRender.drawSolidArea(g2, p, screenX, screenY);
            }
        } 
        else if (obj instanceof model.entity.Monk m) {
            monkRenderer.draw(g2, m, screenX, screenY);
            if (debugMode) {
                monkRenderer.drawSolidArea(g2, m, screenX, screenY);
            }
        } 
        else if (obj instanceof model.entity.EnemyTNT tnt) {
            tntRenderer.draw(g2, tnt, screenX, screenY);
            if (debugMode) {
                tntRenderer.drawSolidArea(g2, tnt, screenX, screenY);
            }
        } 
        else if (obj instanceof model.entity.EnemyDynamite dynamite) {
            dynamiteRender.draw(g2, dynamite, screenX, screenY);
            if (debugMode) {
                dynamiteRender.drawSolidArea(g2, dynamite, screenX, screenY);
            }
        } 
        else if (obj instanceof model.entity.EnemyTorch torch) {
            torchRenderer.draw(g2, torch, screenX, screenY);
            if (debugMode) {
                torchRenderer.drawSolidArea(g2, torch, screenX, screenY);
            }
        } 
        else if (obj instanceof model.entity.DynamiteProjectile proj) {
            dynamiteRender.drawProjectile(g2, proj, screenX, screenY);
            if (debugMode) {
                dynamiteRender.drawProjectileSolidArea(g2, proj, screenX, screenY);
            }
        } 
        else if (obj instanceof model.object.GameObject o) {
            objectRenderer.draw(g2, o, screenX, screenY);
            if (debugMode) {
                objectRenderer.drawDebugObject(g2, o, screenX, screenY, playerCurrentLayer);
            }
        }
    }
    
    /**
     * Updates the animations of all entities and game objects in the model.
     */
    public void update(IController controller, double deltaMs) {
        for (IRenderable obj : controller.getAllRenderables()) {
            if (obj instanceof model.entity.Player p)          playerRender.update(p, deltaMs);
            else if (obj instanceof model.entity.Monk m)       monkRenderer.update(m, deltaMs);
            else if (obj instanceof model.entity.EnemyTNT tnt) tntRenderer.update(tnt, deltaMs);
            else if (obj instanceof model.entity.EnemyDynamite ed) dynamiteRender.update(ed, deltaMs);
            else if (obj instanceof model.entity.EnemyTorch et) torchRenderer.update(et, deltaMs);
            else if (obj instanceof model.object.GameObject o)  objectRenderer.update(o, deltaMs);
        }
    }
    
    public void updatePlayerColor(PlayerColor color) {
        playerRender.setPlayerColor(color);
    }
}