package view;

import main.GameSetting;
import model.GameModel;

import javax.swing.*;
import java.awt.*;

public class GameView extends JPanel {
    // ALL THE RENDERING STAFF HERE
    // render, camera, asset( img animation, texture), sfx ...


    private GameModel model;
    private GameSetting gs;

    public GameView(GameModel model, GameSetting setting) {
        this.model = model;
        this.gs = setting;

        this.setPreferredSize (new Dimension(gs.SCREEN_WIDTH, gs.SCREEN_HEIGHT)) ;
        this.setBackground (Color.black) ;
        this.setDoubleBuffered (true) ;
        this.setFocusable(true);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw the world
        model.getWorld().draw(g2);

        // Draw the player
        model.getPlayer().draw(g2);

        g2.dispose();
    }
}


