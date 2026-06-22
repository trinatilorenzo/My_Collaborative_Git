package tinyswordsisland.view.renderer;

import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.enu.PlayerColor;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.view.renderer.entity.*;

import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RenderDispatcher {

    private final Map<Class<?>, RenderableDrawHandler<?>> handlers = new LinkedHashMap<>();
    private final PlayerRender playerRender;
    private final GameObjectRenderer objectRenderer;

    public RenderDispatcher(GameConfig GS, PlayerColor initialPlayerColor) {
        this.playerRender = new PlayerRender(GS.entityConfig(), initialPlayerColor);
        this.objectRenderer = new GameObjectRenderer();
        MonkRenderer monkRenderer = new MonkRenderer(GS.entityConfig());
        TNTRenderer tntRenderer = new TNTRenderer(GS.entityConfig());
        DynamiteRender dynamiteRender = new DynamiteRender(GS.entityConfig());
        TorchRenderer torchRenderer = new TorchRenderer(GS.entityConfig());

        register(Player.class, new RenderableDrawHandler<>() {
            @Override public Class<Player> getType() { return Player.class; }
            @Override public void draw(Graphics2D g2, Player p, int sx, int sy) { playerRender.draw(g2, p, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, Player p, int sx, int sy, int layer) { playerRender.drawSolidArea(g2, p, sx, sy); }
            @Override public void update(Player p, double d) { playerRender.update(p, d); }
        });
        register(Monk.class, new RenderableDrawHandler<>() {
            @Override public Class<Monk> getType() { return Monk.class; }
            @Override public void draw(Graphics2D g2, Monk m, int sx, int sy) { monkRenderer.draw(g2, m, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, Monk m, int sx, int sy, int layer) { monkRenderer.drawSolidArea(g2, m, sx, sy); }
            @Override public void update(Monk m, double d) { monkRenderer.update(m, d); }
        });
        register(EnemyTNT.class, new RenderableDrawHandler<>() {
            @Override public Class<EnemyTNT> getType() { return EnemyTNT.class; }
            @Override public void draw(Graphics2D g2, EnemyTNT t, int sx, int sy) { tntRenderer.draw(g2, t, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, EnemyTNT t, int sx, int sy, int layer) { tntRenderer.drawSolidArea(g2, t, sx, sy); }
            @Override public void update(EnemyTNT t, double d) { tntRenderer.update(t, d); }
        });
        register(EnemyDynamite.class, new RenderableDrawHandler<>() {
            @Override public Class<EnemyDynamite> getType() { return EnemyDynamite.class; }
            @Override public void draw(Graphics2D g2, EnemyDynamite e, int sx, int sy) { dynamiteRender.draw(g2, e, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, EnemyDynamite e, int sx, int sy, int layer) { dynamiteRender.drawSolidArea(g2, e, sx, sy); }
            @Override public void update(EnemyDynamite e, double d) { dynamiteRender.update(e, d); }
        });
        register(EnemyTorch.class, new RenderableDrawHandler<>() {
            @Override public Class<EnemyTorch> getType() { return EnemyTorch.class; }
            @Override public void draw(Graphics2D g2, EnemyTorch t, int sx, int sy) { torchRenderer.draw(g2, t, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, EnemyTorch t, int sx, int sy, int layer) { torchRenderer.drawSolidArea(g2, t, sx, sy); }
            @Override public void update(EnemyTorch t, double d) { torchRenderer.update(t, d); }
        });
        register(DynamiteProjectile.class, new RenderableDrawHandler<>() {
            @Override public Class<DynamiteProjectile> getType() { return DynamiteProjectile.class; }
            @Override public void draw(Graphics2D g2, DynamiteProjectile p, int sx, int sy) { dynamiteRender.drawProjectile(g2, p, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, DynamiteProjectile p, int sx, int sy, int layer) { dynamiteRender.drawProjectileSolidArea(g2, p, sx, sy); }
            @Override public void update(DynamiteProjectile p, double d) { }
        });
        register(GameObject.class, new RenderableDrawHandler<>() {
            @Override public Class<GameObject> getType() { return GameObject.class; }
            @Override public void draw(Graphics2D g2, GameObject o, int sx, int sy) { objectRenderer.draw(g2, o, sx, sy); }
            @Override public void drawDebug(Graphics2D g2, GameObject o, int sx, int sy, int layer) { objectRenderer.drawDebugObject(g2, o, sx, sy, layer); }
            @Override public void update(GameObject o, double d) { objectRenderer.update(o, d); }
        });
    }

    private <T extends IRenderable> void register(Class<T> type, RenderableDrawHandler<T> handler) {
        handlers.put(type, handler);
    }

    public void draw(Graphics2D g2, IRenderable obj, int screenX, int screenY,
                     boolean debugMode, int playerCurrentLayer) {
        RenderableDrawHandler<IRenderable> handler = findHandler(obj);
        if (handler == null) return;
        handler.draw(g2, obj, screenX, screenY);
        if (debugMode) {
            handler.drawDebug(g2, obj, screenX, screenY, playerCurrentLayer);
        }
    }

    public void update(List<IRenderable> renderables, double deltaMs) {
        for (IRenderable obj : renderables) {
            RenderableDrawHandler<IRenderable> handler = findHandler(obj);
            if (handler != null) {
                handler.update(obj, deltaMs);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private RenderableDrawHandler<IRenderable> findHandler(IRenderable obj) {
        for (Map.Entry<Class<?>, RenderableDrawHandler<?>> entry : handlers.entrySet()) {
            if (entry.getKey().isInstance(obj)) {
                return (RenderableDrawHandler<IRenderable>) entry.getValue();
            }
        }
        return null;
    }

    public void updatePlayerColor(PlayerColor color) {
        playerRender.setPlayerColor(color);
    }
}
