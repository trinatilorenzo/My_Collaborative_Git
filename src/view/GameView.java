package view;

import model.GameModel;
import model.object.GameObject;
import model.object.OBJ_Tree;
import model.entity.Player;
import view.renderer.entity.PlayerRender;
import view.renderer.map.MapRender;
import view.renderer.map.TileSet;

import view.renderer.object.TreeRenderer;
import view.renderer.object.ObjectRender;
import view.renderer.object.RendererRegistry;


import javax.swing.*;
import java.awt.*;

import static main.GameSetting.*;

/**
 * ALL THE RENDERING STAFF HERE
 * render, camera, asset( img animation, texture), sfx ..
 * GameView is responsible for rendering the game to the screen. This class extends JPanel
 * and contains methods and assets required for drawing the game world, player, and other
 * visual elements.
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameView extends JPanel {

    private GameModel model;
    private MapRender mapRender;
    private TileSet tileSet;
    private PlayerRender playerRender;
    private UI ui_render;
    private RendererRegistry rendererRegistry;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameView(GameModel model) {
        this.model = model;
        this.mapRender = new MapRender();

        this.setPreferredSize (new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT)) ;
        this.setBackground (Color.black) ;
        this.setDoubleBuffered (true) ;
        this.setFocusable(true);

        //  import the tileset Asset
        this.tileSet = new TileSet(TILESET_PATH, ORIGINAL_TILE_SIZE, MAX_TILESET_RAW, MAX_TILESET_COL);

        // import the player Render
        this.playerRender = new PlayerRender();

        //import the UI
        this.ui_render = new UI(model, playerRender, mapRender);

        // object renderers
        this.rendererRegistry = new RendererRegistry();
        rendererRegistry.register(OBJ_Tree.class, new TreeRenderer());
        // Aggiungi altri renderer qui quando disponibili
        
        //TODO: import other asset (object, npc, monster)
          
    }
    //-------------------------------------------------------------


    // where everything will be drawn
    //-------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Always clear background
        g2.setColor(GAME_BG_COLOR);
        g2.fillRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // DRAW THE WORLD (anche in pausa, usando l'ultimo stato)
        mapRender.DrawMap(model.getWorldMap(), tileSet, model.getPlayer(), g2);
        
        //DRAW OBJECTS
        drawObjects(g2);

        // DRAW THE PLAYER
        playerRender.draw(g2, model.getPlayer());

        // DRAW THE UI
        ui_render.draw(g2);

        //g2.dispose(); // not necessary
    }

    //-------------------------------------------------------------
    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        playerRender.updateAnimations(model.getPlayer(), deltaMs);
        updateObjectAnimations(deltaMs);

    }

    //-------------------------------------------------------------

    // GETTER ----------------------
    public PlayerRender getPlayerRender() {return playerRender;}
    //---------------------------------

    private void drawObjects(Graphics2D g2) {
        Player player = model.getPlayer();

        for (GameObject obj : model.getObjects()) {
            @SuppressWarnings("unchecked")
            ObjectRender<GameObject> renderer = (ObjectRender<GameObject>) rendererRegistry.getRenderer(obj.getClass());
            if (renderer == null) continue;

            int screenX = obj.getWorldX() - player.getWorldX() + player.getScreenX();
            int screenY = obj.getWorldY() - player.getWorldY() + player.getScreenY();

            // basic culling to avoid drawing off-screen
            if (screenX + TILE_SIZE < 0 || screenX > SCREEN_WIDTH || screenY + TILE_SIZE < 0 || screenY > SCREEN_HEIGHT) {
                continue;
            }

            renderer.draw(g2, obj, screenX, screenY);
        }
    }

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
