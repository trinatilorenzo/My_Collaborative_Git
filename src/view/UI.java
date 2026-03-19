package view;

import model.GameModel;
import view.renderer.entity.PlayerRender;
import view.renderer.map.MapRender;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static main.GameSetting.*;

public class UI {
    GameModel gameModel;
    Graphics2D g2;

    private PlayerRender playerRender;
    private MapRender mapRender;

    Font arial_40;
    Font arial_80B;

    // FPS counter (updated once per second)
    private long fpsTimer = System.nanoTime();
    private int frames = 0;
    private int fps = 0;


    public UI(GameModel gameModel, PlayerRender playerRender, MapRender mapRender) {
        this.gameModel = gameModel;
        this.playerRender = playerRender;
        this.mapRender =  mapRender;

        arial_40 = new Font ("Arial", Font. PLAIN, 40) ;
        arial_80B = new Font ("Arial", Font. BOLD, 80);


    }


    // This method is inside the GAME LOOP --> will be called 60 time per second ! be careful
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.white);

        switch (gameModel.getGameState()) {

            case PLAYING :
                //PLAY STATE
                break;

            case PAUSED :
                // PAUSE STATE
                drawPauseScreen();
                break;
        }

        if (gameModel.isDebugMode()) {
            playerRender.drawSolidArea(g2, gameModel.getPlayer());
            mapRender.drawAllGameLayers(gameModel.getWorldMap(), gameModel.getPlayer(), g2);

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

    }

    private void drawPauseScreen() {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";

        int x = getXforCenteredText(text);
        int y = SCREEN_HEIGHT / 2;

        g2.drawString(text, x, y);
    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = SCREEN_WIDTH / 2 - length / 2;
        return x;
    }



}