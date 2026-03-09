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
        tileSet.updateAnimTile();

        // DRAW THE PLAYER
        playerRender.draw(g2, model.getPlayer());


        //g2.dispose(); // not necessary
    }

    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------

