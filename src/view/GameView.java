package view;

import main.GameSetting;
import model.GameModel;

import javax.swing.*;
import java.awt.*;

import static main.GameSetting.*;

public class GameView extends JPanel {
    // ALL THE RENDERING STAFF HERE
    // render, camera, asset( img animation, texture), sfx ...


    private GameModel model;

    public GameView(GameModel model) {
        this.model = model;

        this.setPreferredSize (new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT)) ;
        this.setBackground (Color.black) ;
        this.setDoubleBuffered (true) ;
        this.setFocusable(true);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw the world
        g2.setColor(GAME_BG_COLOR);
        g2.fillRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);
       model.getWorld().DrawMap(g2);

        // Draw the player
        model.getPlayer().draw(g2);


        g2.dispose();
    }
}


