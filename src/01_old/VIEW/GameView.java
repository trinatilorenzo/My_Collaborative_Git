package old.VIEW;


import old.CONTROLLER.GameController;
import old.GameSetting;
import old.MODEL.GameModel;

import old.VIEW.tile.TileManager;

import javax.swing.*;
import java.awt.*;

public class GameView extends JPanel {
    // ALL THE RENDERING STAFF HERE
    // render, camera, asset( img animation, texture), sfx ...

    private final GameModel model;
    private final GameController controller;
    private GameSetting gs;


    TileManager tileM = new TileManager(gs);

    public GameView(GameModel model, GameController controller ,GameSetting gs) {
        this.model = model;
        this.controller = controller;
        this.gs = gs;

        this.setPreferredSize (new Dimension(gs.SCREEN_WIDTH, gs.SCREEN_HEIGHT)) ;
        this.setBackground (Color.black) ;
        this.setDoubleBuffered (true) ;
        this.setFocusable(true);
        this.addKeyListener(controller);

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        tileM.draw(g2);
        model.player.draw(g2);

        g2.dispose();

    }
}
