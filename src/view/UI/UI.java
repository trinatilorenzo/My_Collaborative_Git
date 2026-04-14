package view.UI;

import model.GameModel;
import model.entity.EnemyTNT;
import model.entity.Monk;
import model.entity.Player;
import view.renderer.entity.MonkRenderer;
import view.renderer.entity.PlayerRender;
import view.renderer.entity.TNTRenderer;
import view.renderer.map.MapRender;
import main.CONFIG.ScreenConfig;
import main.CONFIG.MapConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

//TODO sintassi commenti e revisione codice
public class UI {
    GameModel gameModel;
    Graphics2D g2;

    private PlayerRender playerRender;
    private TNTRenderer tntRenderer;
    private MonkRenderer monkRenderer;
    private MapRender mapRender;
    private final ScreenConfig screenConfig;
    private final MapConfig mapConfig;

    Font arial_40;
    Font arial_80B;

    Font DungeonFont;
    Font MaruMonica;

    private final BufferedImage heartFull;
    private final BufferedImage heartHalf;
    private final BufferedImage heartBlank;

    // FPS counter (updated once per second)
    private long fpsTimer = System.nanoTime();
    private int frames = 0;
    private int fps = 0;


    public UI(GameModel gameModel, PlayerRender playerRender, MapRender mapRender,
              ScreenConfig screenConfig, MapConfig mapConfig, TNTRenderer tntRenderer, MonkRenderer monkRenderer) {
        this.gameModel = gameModel;
        this.playerRender = playerRender;
        this.mapRender =  mapRender;
        this.screenConfig = screenConfig;
        this.mapConfig = mapConfig;

        this.tntRenderer = tntRenderer;
        this.monkRenderer = monkRenderer;

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

        heartFull = scaleImage(loadUiImage("src/res/UI/heart_full.png"),
                screenConfig.TILE_SIZE(), screenConfig.TILE_SIZE());
        heartHalf = scaleImage(loadUiImage("src/res/UI/heart_half.png"),
                screenConfig.TILE_SIZE(), screenConfig.TILE_SIZE());
        heartBlank = scaleImage(loadUiImage("src/res/UI/heart_blank.png"),
                screenConfig.TILE_SIZE(), screenConfig.TILE_SIZE());


    }


    // This method is inside the GAME LOOP --> will be called 60 time per second ! be careful
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.white);

        switch (gameModel.getGameState()) {

            case PLAYING :
                //PLAY STATE
                drawPlayerLife();
                if (!gameModel.getCurrentDialogue().isEmpty()){
                    drawDialogueWindow();
                }
                break;

            case PAUSED :
                // PAUSE STATE
                drawPlayerLife();
                drawPauseScreen();
                break;
        }

        if (gameModel.isDebugMode()) {
            playerRender.drawSolidArea(g2, gameModel.getPlayer());

            mapRender.drawAllGameLayers(gameModel.getWorldMap(), gameModel.getPlayer(), g2);
            List<Object> renderList = new ArrayList<>();
            renderList.add(gameModel.getPlayer());
            renderList.add(gameModel.getMonk());
            renderList.addAll(gameModel.getTntEnemies());

            for (Object obj : renderList) {
                if (obj instanceof Player p) {
                    playerRender.drawSolidArea(g2, p);
                }
                if (obj instanceof Monk m) {
                   // monkRenderer.drawSolidArea(g2, m);
                }
                if (obj instanceof EnemyTNT t){
                    //tntRenderer.drawSolidArea(g2, t, gameModel.getPlayer());
                }

            }
            for (EnemyTNT tnt : gameModel.getTntEnemies()) {
                tntRenderer.drawSolidArea(g2, tnt, tnt.getWorldX() - gameModel.getPlayer().getWorldX() + gameModel.getPlayer().getScreenX(),
                tnt.getWorldY() - gameModel.getPlayer().getWorldY() + gameModel.getPlayer().getScreenY()
                );
            }




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
            int xTile = (gameModel.getPlayer().getWorldX() + gameModel.getPlayer().getSolidArea().x )/ screenConfig.TILE_SIZE();
            int yTile = (gameModel.getPlayer().getWorldY() + gameModel.getPlayer().getSolidArea().y )/ screenConfig.TILE_SIZE();
            g2.drawString("FPS: " + fps + " PLAYER X: "+xTile+", Y:"+yTile+" L: "+gameModel.getPlayer().getCurrentLayer(), 10, 18);
        }

    }

    private BufferedImage loadUiImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load UI image: " + path, e);
        }
    }

    private void drawPlayerLife() {
        int playerLife = gameModel.getPlayer().getLife();
        int maxLife = gameModel.getPlayer().getMaxLife();
        int totalHearts = (maxLife + 1) / 2;

        int heartWidth = heartFull.getWidth();
        int heartHeight = heartFull.getHeight();
        int x = 20;
        int y = 20;
        int spacing = Math.max(4, heartWidth / 6);

        for (int i = 0; i < totalHearts; i++) {
            int lifeForHeart = playerLife - (i * 2);
            BufferedImage heartImage;

            if (lifeForHeart >= 2) {
                heartImage = heartFull;
            } else if (lifeForHeart == 1) {
                heartImage = heartHalf;
            } else {
                heartImage = heartBlank;
            }

            g2.drawImage(heartImage, x + (i * (heartWidth + spacing)), y, null);
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

    public void drawDialogueWindow() {
       // Parametri finestra (Simile alla pausa ma in basso)
        int width = screenConfig.SCREEN_WIDTH() - (screenConfig.TILE_SIZE() * 2);
        int height = screenConfig.TILE_SIZE() * 4;
        int x = (screenConfig.SCREEN_WIDTH() - width) / 2;
        int y = screenConfig.SCREEN_HEIGHT() - height - screenConfig.TILE_SIZE();

        // Disegno Banner (ThreeSliceSprite)
        ThreeSliceSprite diagSprite = new ThreeSliceSprite("src/res/UI/Banners/Banner_Horizontal.png", 64, 64);
        diagSprite.draw(g2, x, y, width);

        // Testo
        g2.setColor(new Color(60, 40, 20)); // Colore scuro per pergamena
        g2.setFont(MaruMonica.deriveFont(Font.BOLD, 28F));
        
        int textX = x + 60;
        int textY = y + 80;

        // Disegno riga per riga (se usi \n nel testo)
        String dialogue = gameModel.getCurrentDialogue();
        if (dialogue == null || dialogue.isEmpty()) return;

        for (String line : dialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }
        
        // Indicatore per il giocatore
        g2.setFont(MaruMonica.deriveFont(Font.ITALIC, 22F));
        g2.drawString("Premi M per continuare...", x + width - 300, y + height - 130);
    }

    public BufferedImage scaleImage(BufferedImage original, int width, int height ){
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics(); // andrà a disegnarlo in scale image
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();

        return scaledImage;
    }

}
