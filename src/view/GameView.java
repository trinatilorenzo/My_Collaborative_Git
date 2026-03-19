package view;

import model.GameModel;
import model.object.GameObject;
import view.renderer.entity.PlayerRender;
import view.renderer.map.MapRender;
import view.renderer.map.TileSet;

import view.renderer.object.TreeRenderer;


import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

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

        //this.rendererRegistry = new ObjectRendererRegistry();
        //rendererRegistry.register(OBJ_Tree.class, new TreeRenderer());
        // Aggiungi altri: rendererRegistry.register(OBJ_Rock.class, new RockRenderer());
        
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

    }

    //-------------------------------------------------------------

    // GETTER ----------------------
    public PlayerRender getPlayerRender() {return playerRender;}
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
