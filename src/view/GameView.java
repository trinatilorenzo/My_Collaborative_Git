package view;

import main.CONFIG.GameConfig;
import main.CONFIG.ScreenConfig;
import main.CONFIG.EntityConfig;
import main.CONFIG.enu.GameState;
import model.GameModel;
import model.object.GameObject;
import model.entity.EnemyDynamite;
import model.entity.EnemyTNT;
import model.entity.Monk;
import model.object.OBJ_Tree;
import model.entity.Player;
import view.UI.UI;
import view.renderer.entity.PlayerRender;
import view.renderer.map.MapRender;
import view.renderer.map.TileSet;

import view.renderer.object.TreeRenderer;
import view.renderer.object.ObjectRender;
import view.renderer.object.RendererRegistry;
import view.renderer.entity.DynamiteRender;
import view.renderer.entity.MonkRenderer;
import view.renderer.entity.TNTRenderer;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


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

    private GameModel model;
    private MapRender mapRender;
    private TileSet tileSet;
    private PlayerRender playerRender;
    private MonkRenderer monkRenderer;
    private TNTRenderer tntRenderer;
    private DynamiteRender enemyDynamiteRender;
    private UI ui_render;
    private RendererRegistry rendererRegistry;
    private Cursor customGameCursor;


    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameView(GameConfig GS, GameModel model) {
        this.screenCfg = GS.screenConfig();

        this.model = model;
        this.mapRender = new MapRender();

        this.setPreferredSize (new Dimension(screenCfg.SCREEN_WIDTH(), screenCfg.SCREEN_HEIGHT()));
        this.setBackground (Color.black) ;
        this.setDoubleBuffered (true) ;
        this.setFocusable(true);
        applyCustomCursor();

        //  import the tileset Asset
        //TODO take only tilesetCOnfig
        this.tileSet = new TileSet(GS.TILESET_PATH, GS.mapConfig().ORIGINAL_TILESIZE(), GS.mapConfig().MAX_TILESET_ROW, GS.mapConfig().MAX_TILESET_COL);

        // import the player Render
        this.playerRender = new PlayerRender(GS.entityConfig());

        this.monkRenderer = new MonkRenderer(GS.entityConfig());
        this.tntRenderer = new TNTRenderer(GS.entityConfig());
        this.enemyDynamiteRender = new DynamiteRender(GS.entityConfig());

        //import the UI
        this.ui_render = new UI(model, playerRender, mapRender,screenCfg, GS.mapConfig(), tntRenderer, monkRenderer);

        // object renderers
        this.rendererRegistry = new RendererRegistry();
        rendererRegistry.register(OBJ_Tree.class, new TreeRenderer());
        
        //TODO: import other asset (object, npc, monster)
          
    }
    //-------------------------------------------------------------

    public UI.MainMenuLayout getMainMenuLayout() {
        return ui_render.getMainMenuLayout();
    }

    private void applyCustomCursor() {
        try {
            BufferedImage cursorImage = null;
            try (InputStream is = getClass().getResourceAsStream("/res/UI/Pointers/01.png")) {
                if (is != null) {
                    cursorImage = ImageIO.read(is);
                }
            }
            if (cursorImage == null) {
                cursorImage = ImageIO.read(new File("src/res/UI/Pointers/01.png"));
            }
            customGameCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "game_cursor");
            setCursor(customGameCursor);
        } catch (IOException e) {
            // If cursor asset cannot be loaded, keep default cursor.
        }
    }

    public Cursor getCustomGameCursor() {
        return customGameCursor;
    }

    // where everything will be drawn
    //-------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //TODO pulire la logica di rendering

        // Always clear background
        g2.setColor(screenCfg.GAME_BG_COLOR());
        g2.fillRect(0,0, screenCfg.SCREEN_WIDTH(), screenCfg.SCREEN_HEIGHT());

        if (model.getGameState() != GameState.MENU) {
            // DRAW THE WORLD (anche in pausa, usando l'ultimo stato)
            mapRender.DrawMap(screenCfg, model.getWorldMap(), tileSet, model.getPlayer(), g2);

            // Y-sorting logic: sort objects by their worldY coordinate
            drawEntities(g2);
        }

        //DRAW OBJECTS
        //drawObjects(g2);

        // DRAW THE PLAYER
        //playerRender.draw(g2, model.getPlayer());

        // DRAW THE UI
        ui_render.draw(g2);

        //g2.dispose(); // not necessary
    }

    //-------------------------------------------------------------

    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        playerRender.updateAnimations(model.getPlayer(), deltaMs);
        monkRenderer.update(model.getMonk(), deltaMs);
        for (EnemyTNT tnt : model.getTntEnemies()) {
            tntRenderer.update(tnt, deltaMs);
        }
        for (EnemyDynamite enemyDynamite : model.getDynamiteEnemies()){
            enemyDynamiteRender.update(enemyDynamite, deltaMs);
        }
        updateObjectAnimations(deltaMs);


    }
    //-------------------------------------------------------------

    //--------------------------------------------------------------
    private void drawEntities(Graphics2D g2) {
        Player player = model.getPlayer();
        Monk monk = model.getMonk();

        // List to hold all entities for sorting
        java.util.List<Object> renderList = new java.util.ArrayList<>();

        renderList.add(player);
        renderList.add(monk);
        renderList.addAll(model.getTntEnemies());
        renderList.addAll(model.getDynamiteEnemies());
        renderList.addAll(model.getObjects());

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
            } else if (obj instanceof EnemyDynamite d) {
                return d.getWorldY() + d.getSolidArea().height / 2;
            }
            return 0;
        }));

        for (Object obj : renderList) {

            if (obj instanceof Player p) {
                playerRender.draw(g2, p);
            }
            if (obj instanceof Monk m) {
                int screenX = m.getWorldX() - player.getWorldX() + player.getScreenX();
                int screenY = m.getWorldY() - player.getWorldY() + player.getScreenY();
                monkRenderer.draw(g2, m, screenX, screenY);
            }
            else if (obj instanceof EnemyTNT tnt) {
                int screenX = tnt.getWorldX() - player.getWorldX() + player.getScreenX();
                int screenY = tnt.getWorldY() - player.getWorldY() + player.getScreenY();

                int halfW = EntityConfig.TNT_SPRITE_WIDTH / 2;
                int halfH = EntityConfig.TNT_SPRITE_HEIGHT / 2;
                if (screenX + halfW < 0 || screenX - halfW > screenCfg.SCREEN_WIDTH() ||
                    screenY + halfH < 0 || screenY - halfH > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }

                tntRenderer.draw(g2, tnt, screenX, screenY);
            }
            else if (obj instanceof EnemyDynamite enemyDynamite) {
                int screenX = enemyDynamite.getWorldX() - player.getWorldX() + player.getScreenX();
                int screenY = enemyDynamite.getWorldY() - player.getWorldY() + player.getScreenY();
                int halfW = EntityConfig.DYNAMITE_SPRITE_WIDTH / 2;
                int halfH = EntityConfig.DYNAMITE_SPRITE_HEIGHT / 2;
                if (screenX + halfW < 0 || screenX - halfW > screenCfg.SCREEN_WIDTH() ||
                    screenY + halfH < 0 || screenY - halfH > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }
                enemyDynamiteRender.draw(g2, enemyDynamite, screenX, screenY);


            }

            else if (obj instanceof GameObject o) {
                @SuppressWarnings("unchecked")
                ObjectRender<GameObject> renderer =
                        (ObjectRender<GameObject>) rendererRegistry.getRenderer(o.getClass());

                if (renderer == null) continue; 

                int screenX = o.getWorldX() - player.getWorldX() + player.getScreenX();
                int screenY = o.getWorldY() - player.getWorldY() + player.getScreenY();

                // culling: draw only if visible on screen
                if (screenX + o.getWidth() < 0 || screenX > screenCfg.SCREEN_WIDTH() ||
                    screenY + o.getHeight() < 0 || screenY > screenCfg.SCREEN_HEIGHT()) {
                    continue;
                }

                renderer.draw(g2, o, screenX, screenY);
            }
        }
    }
    //--------------------------------------------------------------

    // GETTER ----------------------
    public PlayerRender getPlayerRender() {return playerRender;}
    //---------------------------------

    private void updateObjectAnimations(double deltaMs) {
        for (GameObject obj : model.getObjects()) {
            @SuppressWarnings("unchecked")
            ObjectRender<GameObject> renderer = (ObjectRender<GameObject>) rendererRegistry.getRenderer(obj.getClass());
            if (renderer == null) continue;
            renderer.update(obj, deltaMs);
        }
    }

}
//-------------------------------------------------------------------------------------------------------------------
