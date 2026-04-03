package view;

import model.GameModel;
import view.renderer.entity.PlayerRender;
import view.renderer.map.MapRender;
import main.CONFIG.ScreenConfig;
import main.CONFIG.MapConfig;
import main.ENUM.GameState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class UI {
    GameModel gameModel;
    Graphics2D g2;

    private PlayerRender playerRender;
    private MapRender mapRender;
    private final ScreenConfig screenConfig;
    private final MapConfig mapConfig;

    Font arial_40;
    Font arial_80B;

    Font DungeonFont;
    Font MaruMonica;

    // FPS counter (updated once per second)
    private long fpsTimer = System.nanoTime();
    private int frames = 0;
    private int fps = 0;


    public UI(GameModel gameModel, PlayerRender playerRender, MapRender mapRender,
              ScreenConfig screenConfig, MapConfig mapConfig) {
        this.gameModel = gameModel;
        this.playerRender = playerRender;
        this.mapRender =  mapRender;
        this.screenConfig = screenConfig;
        this.mapConfig = mapConfig;

        arial_40 = new Font ("Arial", Font. PLAIN, 40) ;
        arial_80B = new Font ("Arial", Font. BOLD, 80);

        InputStream is = getClass().getResourceAsStream("/res/fonts/x12y16pxMaruMonica.ttf");
        try {
            MaruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        is = getClass().getResourceAsStream("/res/fonts/DungeonFont.ttf");
        try {
            DungeonFont = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



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
        // BG
        g2.setColor(screenConfig.GAME_BG_COLOR());
        g2.fillRect(0, 0, screenConfig.SCREEN_WIDTH(), screenConfig.SCREEN_HEIGHT());

        ThreeSliceSprite pauseSprite = new ThreeSliceSprite("src/res/UI/Banners/Banner_Horizontal.png", 192/3, 192/3);

        int bannerWidth = 192 * 3; // desired logical width
        int bannerHeight = pauseSprite.getHeight();

        int bannerX = (screenConfig.SCREEN_WIDTH() - bannerWidth) / 2;
        int bannerY = (screenConfig.SCREEN_HEIGHT() - bannerHeight) / 2;

        pauseSprite.draw(g2, bannerX, bannerY, bannerWidth);

        // PAUSE TITLE
        g2.setColor(Color.WHITE);
        g2.setFont(MaruMonica.deriveFont(Font.BOLD, 80));
        String text = "PAUSED";

        int x = getXforCenteredText(text);
        int y = screenConfig.SCREEN_HEIGHT() / 2;

        g2.drawString(text, x, y);


    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = screenConfig.SCREEN_WIDTH() / 2 - length / 2;
        return x;
    }



}
