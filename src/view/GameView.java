package view;

import model.GameModel;
import view.renderMap.MapRender;
import view.renderMap.TileSet;
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

    private boolean debugMode = false;

    // FPS counter (updated once per second)
    private long fpsTimer = System.nanoTime();
    private int frames = 0;
    private int fps = 0;

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
    }
    //-------------------------------------------------------------


    // where everything will be drawn
    //-------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // DRAW THE WORLD
        g2.setColor(GAME_BG_COLOR);
        g2.fillRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);

        mapRender.DrawMap(model.getWorldMap(), tileSet, model.getPlayer(), g2);

        // DRAW THE PLAYER
        playerRender.draw(g2, model.getPlayer());

        //debug mod
        if (debugMode) {
            playerRender.drawSolidArea(g2, model.getPlayer());
            mapRender.drawAllGameLayers(model.getWorldMap(), model.getPlayer(), g2);

            // FPS overlay (updates every second)
            frames++;
            long now = System.nanoTime();
            if (now - fpsTimer >= 1_000_000_000L) {
                fps = frames;
                frames = 0;
                fpsTimer = now;
            }

            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            g2.drawString("FPS: " + fps, 10, 18);
        }


        //g2.dispose(); // not necessary
    }

    //-------------------------------------------------------------
    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        playerRender.updateAnimations(model.getPlayer(), deltaMs);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------

    // GETTER ----------------------
    public PlayerRender getPlayerRender() {return playerRender;}
    //---------------------------------

    //DEBUG MODE
    //-------------------------------------------------------------

    public void setDebugModeON() {
        this.debugMode = true;
    }
    public void setDebugModeOFF() {
        this.debugMode = false;
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
