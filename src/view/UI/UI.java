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
import java.awt.geom.Rectangle2D;
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
    private final ThreeSliceSprite menuButton;
    private final ThreeSliceSprite menuButtonSelected;
    private final BufferedImage menuLogo;
    private final BufferedImage settingsIcon;
    private final BufferedImage settingsIconPressed;
    private final BufferedImage ribbonYellow;
    private final BufferedImage ribbonRed;
    private final BufferedImage ribbonBlue;
    private final BufferedImage ribbonYellowPressed;
    private final BufferedImage ribbonRedPressed;
    private final BufferedImage ribbonBluePressed;

    // FPS counter (updated once per second)
    private long fpsTimer = System.nanoTime();
    private int frames = 0;
    private int fps = 0;

    public static final class MainMenuLayout {
        private final Rectangle newGameBounds;
        private final Rectangle continueBounds;
        private final Rectangle settingsBounds;
        private final Rectangle ribbonYellowBounds;
        private final Rectangle ribbonRedBounds;
        private final Rectangle ribbonBlueBounds;

        public MainMenuLayout(Rectangle newGameBounds, Rectangle continueBounds, Rectangle settingsBounds,
                              Rectangle ribbonYellowBounds, Rectangle ribbonRedBounds, Rectangle ribbonBlueBounds) {
            this.newGameBounds = newGameBounds;
            this.continueBounds = continueBounds;
            this.settingsBounds = settingsBounds;
            this.ribbonYellowBounds = ribbonYellowBounds;
            this.ribbonRedBounds = ribbonRedBounds;
            this.ribbonBlueBounds = ribbonBlueBounds;
        }

        public Rectangle newGameBounds() { return newGameBounds; }
        public Rectangle continueBounds() { return continueBounds; }
        public Rectangle settingsBounds() { return settingsBounds; }
        public Rectangle ribbonYellowBounds() { return ribbonYellowBounds; }
        public Rectangle ribbonRedBounds() { return ribbonRedBounds; }
        public Rectangle ribbonBlueBounds() { return ribbonBlueBounds; }
    }


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

        heartFull = scaleImage(loadUiImage("src/res/UI/heart/heart_full.png"),
                screenConfig.TILE_SIZE(), screenConfig.TILE_SIZE());
        heartHalf = scaleImage(loadUiImage("src/res/UI/heart/heart_half.png"),
                screenConfig.TILE_SIZE(), screenConfig.TILE_SIZE());
        heartBlank = scaleImage(loadUiImage("src/res/UI/heart/heart_blank.png"),
                screenConfig.TILE_SIZE(), screenConfig.TILE_SIZE());
        menuButton = new ThreeSliceSprite("src/res/UI/Buttons/Button_Blue_3Slides.png", 21, 21);
        menuButtonSelected = new ThreeSliceSprite("src/res/UI/Buttons/Button_Hover_3Slides.png", 21, 21);
        menuLogo = loadUiImage("src/res/UI/Icons/logo_gioco.png");
        settingsIcon = scaleImage(loadUiImage("src/res/UI/Icons/Regular_02.png"), 56, 56);
        settingsIconPressed = scaleImage(loadUiImage("src/res/UI/Icons/Pressed_02.png"), 56, 56);
        ribbonYellow = loadUiImage("src/res/UI/Ribbons/Ribbon_Yellow_Connection_Right.png");
        ribbonRed = loadUiImage("src/res/UI/Ribbons/Ribbon_Red_Connection_Right.png");
        ribbonBlue = loadUiImage("src/res/UI/Ribbons/Ribbon_Blue_Connection_Right.png");
        ribbonYellowPressed = loadUiImage("src/res/UI/Ribbons/Ribbon_Yellow_Connection_Right_Pressed.png");
        ribbonRedPressed = loadUiImage("src/res/UI/Ribbons/Ribbon_Red_Connection_Right_Pressed.png");
        ribbonBluePressed = loadUiImage("src/res/UI/Ribbons/Ribbon_Blue_Connection_Right_Pressed.png");


    }


    // This method is inside the GAME LOOP --> will be called 60 time per second ! be careful
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.white);

        switch (gameModel.getGameState()) {

            case MENU :
                drawMainMenu();
                break;

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

    private void drawMainMenu() {
        g2.setColor(new Color(28, 30, 34));
        g2.fillRect(0, 0, screenConfig.SCREEN_WIDTH(), screenConfig.SCREEN_HEIGHT());

        int panelMarginX = 0;
        int panelMarginY = 0;
        int panelX = panelMarginX;
        int panelY = panelMarginY;
        int panelW = screenConfig.SCREEN_WIDTH() - (panelMarginX * 2);
        int panelH = screenConfig.SCREEN_HEIGHT() - (panelMarginY * 2);

        GradientPaint backgroundGradient = new GradientPaint(0, 0, new Color(39, 42, 46), screenConfig.SCREEN_WIDTH(), 0, new Color(22, 24, 28));
        g2.setPaint(backgroundGradient);
        g2.fillRect(0, 0, screenConfig.SCREEN_WIDTH(), screenConfig.SCREEN_HEIGHT());

        g2.setColor(new Color(83, 189, 191));
        g2.fillRect(panelX, panelY, panelW, panelH);
        g2.setColor(new Color(140, 224, 228));
        g2.drawRect(panelX, panelY, panelW - 1, panelH - 1);

        int logoWidth = 500;
        int logoHeight = (int) (((double) menuLogo.getHeight() / menuLogo.getWidth()) * logoWidth);
        int logoX = panelX + (panelW - logoWidth) / 2;
        int logoY = panelY + 40;
        g2.drawImage(menuLogo, logoX, logoY, logoWidth, logoHeight, null);

        MainMenuLayout layout = getMainMenuLayout();
        int selectedItem = gameModel.getMainMenuSelection();
        int hoveredRibbon = gameModel.getHoveredRibbon();
        int activeRibbon = gameModel.getActiveRibbon();

        Rectangle newGameBounds = layout.newGameBounds();
        Rectangle continueBounds = layout.continueBounds();
        Rectangle settingsBounds = layout.settingsBounds();
        Rectangle ribbonYellowBounds = layout.ribbonYellowBounds();
        Rectangle ribbonRedBounds = layout.ribbonRedBounds();
        Rectangle ribbonBlueBounds = layout.ribbonBlueBounds();

        g2.drawImage((hoveredRibbon == 0 || activeRibbon == 0) ? ribbonYellowPressed : ribbonYellow,
                ribbonYellowBounds.x, ribbonYellowBounds.y, ribbonYellowBounds.width, ribbonYellowBounds.height, null);
        g2.drawImage((hoveredRibbon == 1 || activeRibbon == 1) ? ribbonRedPressed : ribbonRed,
                ribbonRedBounds.x, ribbonRedBounds.y, ribbonRedBounds.width, ribbonRedBounds.height, null);
        g2.drawImage((hoveredRibbon == 2 || activeRibbon == 2) ? ribbonBluePressed : ribbonBlue,
                ribbonBlueBounds.x, ribbonBlueBounds.y, ribbonBlueBounds.width, ribbonBlueBounds.height, null);

        drawMainMenuOption(newGameBounds.x, newGameBounds.y, newGameBounds.width, newGameBounds.height, "New Game", selectedItem == 0);
        drawMainMenuOption(continueBounds.x, continueBounds.y, continueBounds.width, continueBounds.height, "Resume", selectedItem == 1);

        BufferedImage settingsToDraw = (selectedItem == 2) ? settingsIconPressed : settingsIcon;
        g2.drawImage(settingsToDraw, settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);
    }

    public MainMenuLayout getMainMenuLayout() {
        int buttonWidth = 420;
        int buttonHeight = 96;
        int centerX = screenConfig.SCREEN_WIDTH() / 2;
        int firstY = (screenConfig.SCREEN_HEIGHT() / 2) + 58;
        int verticalGap = 24;

        Rectangle newGameBounds = new Rectangle(centerX - (buttonWidth / 2), firstY, buttonWidth, buttonHeight);
        Rectangle continueBounds = new Rectangle(centerX - (buttonWidth / 2), firstY + buttonHeight + verticalGap, buttonWidth, buttonHeight);

        int settingsSize = 56;
        int settingsX = screenConfig.SCREEN_WIDTH() - settingsSize - 24;
        int settingsY = 24;
        Rectangle settingsBounds = new Rectangle(settingsX, settingsY, settingsSize, settingsSize);

        int ribbonX = 20;
        int ribbonY = 16;
        int ribbonW = 64;
        int ribbonH = 64;
        Rectangle ribbonYellowBounds = new Rectangle(ribbonX, ribbonY, ribbonW, ribbonH);
        Rectangle ribbonRedBounds = new Rectangle(ribbonX, ribbonY + 52, ribbonW, ribbonH);
        Rectangle ribbonBlueBounds = new Rectangle(ribbonX, ribbonY + 104, ribbonW, ribbonH);

        return new MainMenuLayout(newGameBounds, continueBounds, settingsBounds,
                ribbonYellowBounds, ribbonRedBounds, ribbonBlueBounds);
    }

    private void drawMainMenuOption(int x, int y, int width, int height, String label, boolean selected) {
        if (selected) {
            menuButtonSelected.draw(g2, x, y, width, height);
        } else {
            menuButton.draw(g2, x, y, width, height);
        }

        g2.setColor(Color.WHITE);
        g2.setFont(MaruMonica.deriveFont(Font.BOLD, 50F));
        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(label, g2);
        int textX = x + (int) Math.round((width - textBounds.getWidth()) / 2.0 - textBounds.getX());

        // The button sprite has a stronger bottom shadow; center text on the visual body area.
        int contentY = y + 4;
        int contentHeight = height - 35;
        int textY = contentY + (int) Math.round((contentHeight - textBounds.getHeight()) / 2.0 - textBounds.getY());
        g2.drawString(label, textX, textY);
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
