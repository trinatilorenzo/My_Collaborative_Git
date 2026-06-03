package view;

import main.CONFIG.EntityConfig;
import main.CONFIG.GameConfig;
import main.CONFIG.MapConfig;
import main.CONFIG.ScreenConfig;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.GameState;
import main.CONFIG.enu.PlayerColor;
import model.GameModel;
import model.object.GameObject;
import model.entity.DynamiteProjectile;
import model.entity.EnemyDynamite;
import model.entity.EnemyTNT;
import model.entity.Monk;
import model.entity.Player;
import model.entity.EnemyTorch;
import model.event.AudioEventType;
import view.UI.GameOverLayout;
import view.UI.MainMenuLayout;
import view.UI.UI;
import view.audio.GameAudioManager;
import view.renderer.entity.PlayerRender;
import view.renderer.map.MapRender;
import view.renderer.map.TileSet;

import view.renderer.entity.DynamiteRender;
import view.renderer.entity.MonkRenderer;
import view.renderer.entity.TNTRenderer;
import view.renderer.entity.TorchRenderer;
import view.renderer.GameObjectRenderer;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * ALL THE RENDERING STAFF HERE
 * render, camera, asset( img animation, texture), sfx ..
 * GameView is responsible for rendering the game to the screen. This class extends JPanel
 * and contains methods and assets required for drawing the game world, player, and other
 * visual elements.
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameView extends JPanel {

    private final ScreenConfig screenCfg;

    //model to render
    private final GameModel model;

    // renderers
    private final MapRender mapRender;
    private final TileSet tileSet;
    private final PlayerRender playerRender;
    private final MonkRenderer monkRenderer;
    private final TNTRenderer tntRenderer;
    private final DynamiteRender dynamiteRender;
    private final TorchRenderer torchRenderer;
    private final UI ui_render;
    private final GameObjectRenderer objectRenderer;
    private Cursor customGameCursor;

    //audio
    private final GameAudioManager audioManager;


    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameView(GameConfig GS, GameModel model) {
        this.screenCfg = GS.screenConfig();
        this.model = model;

        this.setPreferredSize(new Dimension(screenCfg.SCREEN_WIDTH(), screenCfg.SCREEN_HEIGHT()));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        applyCustomCursor(UIConfig.CURSOR_PATH);

        //  import the tileset Asset
        System.out.println("tileset path: " + GS.TILESET_IMG_PATH());
        this.tileSet = new TileSet(GS.TILESET_IMG_PATH(), GS.mapConfig().ORIGINAL_TILESIZE(), MapConfig.MAX_TILESET_ROW, MapConfig.MAX_TILESET_COL, GS.tilesetDoc());

        //import the map Render
        this.mapRender = new MapRender();

        // import the entity Render
        this.playerRender = new PlayerRender(GS.entityConfig(), EntityConfig.DEFAULT_COLOR);
        this.monkRenderer = new MonkRenderer(GS.entityConfig());
        this.tntRenderer = new TNTRenderer(GS.entityConfig());
        this.dynamiteRender = new DynamiteRender(GS.entityConfig());
        this.torchRenderer = new TorchRenderer(GS.entityConfig());

        // object renderer
        this.objectRenderer = new GameObjectRenderer();
        this.audioManager = new GameAudioManager();
        this.audioManager.syncBackgroundMusic(model.getGameState());

        //import the UI
        this.ui_render = new UI(model, playerRender, mapRender,screenCfg, GS.mapConfig(), tntRenderer, monkRenderer, dynamiteRender, torchRenderer);
    }
    //-------------------------------------------------------------
    private void applyCustomCursor(String cursorPath) {
        try {
            BufferedImage cursorImage = null;
            try (InputStream is = getClass().getResourceAsStream(cursorPath)) {
                if (is != null) {
                    cursorImage = ImageIO.read(is);
                }
            }
            if (cursorImage == null) {
                cursorImage = ImageIO.read(new File(cursorPath));
            }
            customGameCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "game_cursor");
            setCursor(customGameCursor);
        } catch (IOException e) {
            // If cursor asset cannot be loaded, keep default cursor.
        }
    }
    //-------------------------------------------------------------


    /**
     * where everything will be drawn
     */
    //-------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(screenCfg.GAME_BG_COLOR());
        g2.fillRect(0,0, screenCfg.SCREEN_WIDTH(), screenCfg.SCREEN_HEIGHT());

        if (model.getGameState() != GameState.MENU) {

            // DRAW THE WORLD MAP
            mapRender.DrawMap(screenCfg, model.getWorldMap(), tileSet, model.getPlayer(), g2);
            // Y-sorting logic: sort objects by their worldY coordinate
            drawEntities(g2);

            //debug mode --------------
            if (model.isDebugMode()) {
                objectRenderer.drawDebugSolidAreas(g2, model.getObjects(), model.getPlayer(), screenCfg);
            }
            //--------------------------
        }

        // draw the UI
        ui_render.draw(g2);

        g2.dispose();
    }

    //-------------------------------------------------------------

    //--------------------------------------------------------------
    private void drawEntities(Graphics2D g2) {
        Player player = model.getPlayer();
        Monk monk = model.getMonk();

        // Player's coordinates
        int pScreenX = screenCfg.SCREEN_WIDTH() / 2 - (screenCfg.TILE_SIZE() / 2);
        int pScreenY = screenCfg.SCREEN_HEIGHT() / 2 - (screenCfg.TILE_SIZE() / 2);

        // List to hold all entities for sorting
        java.util.List<Object> renderList = new java.util.ArrayList<>();

        renderList.add(player);
        renderList.add(monk);
        renderList.addAll(model.getTntEnemies());
        renderList.addAll(model.getDynamiteEnemies());
        renderList.addAll(model.getProjectiles());
        renderList.addAll(model.getObjects());
        renderList.addAll(model.getTorchEnemies());

        // Sort for "bottom_y"
        renderList.sort(java.util.Comparator.comparingInt(obj -> {
            if (obj instanceof Player p) {
                return p.getWorldY() + p.getSolidArea().height / 2;
            } else if (obj instanceof Monk m) {
                return m.getWorldY() + m.getSolidArea().height / 2;
            } else if (obj instanceof EnemyTNT tnt) {
                return tnt.getWorldY() + tnt.getSolidArea().height / 2;
            } else if (obj instanceof GameObject o) {
                return o.getWorldY() + o.getSolidArea().y + o.getSolidArea().height;
            } else if (obj instanceof EnemyDynamite ed) {
                return ed.getWorldY() + ed.getSolidArea().height / 2;
            } else if (obj instanceof DynamiteProjectile d) {
                return d.getWorldY() + d.getSolidArea().height/2;
            } else if (obj instanceof EnemyTorch torch) {
                return torch.getWorldY() + torch.getSolidArea().height / 2;
            }
            return 0;
        }));

        for (Object obj : renderList) {

            if (obj instanceof Player p) {
                playerRender.draw(g2, p, pScreenX, pScreenY);
            }
            else if (obj instanceof Monk m) {
                int screenX = m.getWorldX() - player.getWorldX() + pScreenX;
                int screenY = m.getWorldY() - player.getWorldY() + pScreenY;
                monkRenderer.draw(g2, m, screenX, screenY);
            }
            else if (obj instanceof EnemyTNT tnt) {
                int screenX = tnt.getWorldX() - player.getWorldX() + pScreenX;
                int screenY = tnt.getWorldY() - player.getWorldY() + pScreenY;

                int halfW = EntityConfig.TNT_SPRITE_WIDTH / 2;
                int halfH = EntityConfig.TNT_SPRITE_HEIGHT / 2;
                if (screenX + halfW < 0 || screenX - halfW > screenCfg.SCREEN_WIDTH() ||
                        screenY + halfH < 0 || screenY - halfH > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }

                tntRenderer.draw(g2, tnt, screenX, screenY);
            }
            else if (obj instanceof EnemyDynamite enemyDynamite) {
                int screenX = enemyDynamite.getWorldX() - player.getWorldX() + pScreenX;
                int screenY = enemyDynamite.getWorldY() - player.getWorldY() + pScreenY;
                int halfW = EntityConfig.DYNAMITE_SPRITE_WIDTH / 2;
                int halfH = EntityConfig.DYNAMITE_SPRITE_HEIGHT / 2;
                if (screenX + halfW < 0 || screenX - halfW > screenCfg.SCREEN_WIDTH() ||
                        screenY + halfH < 0 || screenY - halfH > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }
                dynamiteRender.draw(g2, enemyDynamite, screenX, screenY);
            }
            else if (obj instanceof DynamiteProjectile proj) {
                int screenX = proj.getWorldX() - player.getWorldX() + pScreenX;
                int screenY = proj.getWorldY() - player.getWorldY() + pScreenY;

                int halfW = EntityConfig.PROJECTILE_SPRITE_WIDTH / 2;
                int halfH = EntityConfig.PROJECTILE_SPRITE_HEIGHT / 2;

                if (screenX + halfW < 0 || screenX - halfW > screenCfg.SCREEN_WIDTH() ||
                        screenY + halfH < 0 || screenY - halfH > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }

                dynamiteRender.drawProjectile(g2, proj, screenX, screenY);
            }
            else if (obj instanceof EnemyTorch torch) {
                int screenX = torch.getWorldX() - player.getWorldX() + pScreenX;
                int screenY = torch.getWorldY() - player.getWorldY() + pScreenY;

                int halfW = EntityConfig.TORCH_SPRITE_WIDTH / 2;
                int halfH = EntityConfig.TORCH_SPRITE_HEIGHT / 2;

                if (screenX + halfW < 0 || screenX - halfW > screenCfg.SCREEN_WIDTH() ||
                        screenY + halfH < 0 || screenY - halfH > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }

                torchRenderer.draw(g2, torch, screenX, screenY);
            }
            else if (obj instanceof GameObject o) {
                int screenX = o.getWorldX() - player.getWorldX() + pScreenX;
                int screenY = o.getWorldY() - player.getWorldY() + pScreenY;

                // culling: draw only if visible on screen
                if (screenX + o.getWidth() < 0 || screenX > screenCfg.SCREEN_WIDTH() ||
                        screenY + o.getHeight() < 0 || screenY > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }

                objectRenderer.draw(g2, o, screenX, screenY);
            }
        }
    }
    //--------------------------------------------------------------


    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        playerRender.update(model.getPlayer(), deltaMs);
        monkRenderer.update(model.getMonk(), deltaMs);
        for (EnemyTNT tnt : model.getTntEnemies()) {
            tntRenderer.update(tnt, deltaMs);
        }
        for (EnemyDynamite enemyDynamite : model.getDynamiteEnemies()){
            dynamiteRender.update(enemyDynamite, deltaMs);
        }
        for (EnemyTorch enemyTorch : model.getTorchEnemies()) {
            torchRenderer.update(enemyTorch, deltaMs);
        }
        updateObjectAnimations(deltaMs);


    }
    //-------------------------------------------------------------

    public void setPlayerRenderColor(PlayerColor playerColor) {
        playerRender.setPlayerColor(playerColor);
    }
    //-------------------------------------------------------------

    private void updateObjectAnimations(double deltaMs) {
        for (GameObject obj : model.getObjects()) {
            objectRenderer.update(obj, deltaMs);
        }
    }

    public void onGameStateChanged(GameState gameState) {
        audioManager.syncBackgroundMusic(gameState);
    }

    public void processGameEvents() {
        List<AudioEventType> events = model.consumeAudioEvents();
        if (events.isEmpty()) {
            return;
        }
        for (AudioEventType event : events) {
            if (event == AudioEventType.PLAYER_DAMAGED) {
                ui_render.triggerDamageFlash();
                break;
            }
        }
        audioManager.playEvents(events);
    }

    public void shutdownAudio() {
        audioManager.stopAll();
    }


    // GETTER ----------------------
    public PlayerRender getPlayerRender() {return playerRender;}
    public MainMenuLayout getMainMenuLayout() {
        return ui_render.getMainMenuLayout();
    }
    public GameOverLayout getGameOverLayout() {
        return ui_render.getGameOverLayout();
    }
    public Cursor getCustomGameCursor() {
        return customGameCursor;
    }
    //---------------------------------

    //SETTER ----------------------
    public void setMainMenuSelection(int selection) {
        ui_render.setMainMenuSelection(selection);
    }
    public void setHoveredRibbon(int hoveredRibbon) {
        ui_render.setHoveredRibbon(hoveredRibbon);
    }
    public void setActiveRibbon(int activeRibbon) {
        ui_render.setActiveRibbon(activeRibbon);
    }
    public void setHoveredGameOverButton(boolean hovered) {
        ui_render.setHoveredGameOverButton(hovered);
    }
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
