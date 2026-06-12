package view.UI;

import main.CONFIG.enu.ButtonValue;
import model.GameModel;
import model.entity.Player;
import view.SpriteLoader;
import main.CONFIG.EntityConfig;
import main.CONFIG.ScreenConfig;
import main.CONFIG.UIConfig;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

import static main.CONFIG.enu.ButtonValue.MainMenu.*;
import static main.CONFIG.enu.ButtonValue.Pause.*;
import static main.CONFIG.enu.ButtonValue.Settings.*;
import static main.CONFIG.enu.ButtonValue.GameOver.*;


/**
 * Draws all in-game UI elements: HUD, menus, dialogue windows, and debug HUD overlays.
 */
//-------------------------------------------------------------------------------------------------------------------
public class UI {

    private int screenWidth;
    private int screenHeight;

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final ScreenConfig screenConfig;
    private final GameModel model;
    private Graphics2D g2;

    // =========================================================================
    // Fonts
    // =========================================================================

    private final Font maruMonica;
    private final Font dungeonFont;

    // =========================================================================
    // Assets — loaded once at construction time
    // =========================================================================

    private final BufferedImage heartFull;
    private final BufferedImage heartHalf;
    private final BufferedImage heartBlank;

    private final BufferedImage shield;

    private final SliceSprite menuButton;
    private final SliceSprite menuButtonSelected;
    private final SliceSprite ribbonBlueWide;
    private final SliceSprite pauseBanner;
    private final SliceSprite dialogueBanner;

    private final SliceSprite resumeButton;
    private final SliceSprite resumeButtonSelected;
    private final SliceSprite saveButton;
    private final SliceSprite saveButtonSelected;


    private final BufferedImage buttonMusic;
    private final BufferedImage buttonMusicSelected;
    private final BufferedImage buttonMusicPressed;
    private final BufferedImage buttonSound;
    private final BufferedImage buttonSoundSelected;
    private final BufferedImage buttonSoundPressed;

    private final SliceSprite blueRibbon;
    private final SliceSprite yellowRibbon;
    private final SliceSprite redRibbon;


    private final BufferedImage menuLogo;
    private final BufferedImage settingsIcon;
    private final BufferedImage settingsIconPressed;
    private final BufferedImage avatarYellow;
    private final BufferedImage avatarRed;
    private final BufferedImage avatarBlue;
    private final BufferedImage avatarPurple;
    private final BufferedImage avatarYellowPressed;
    private final BufferedImage avatarRedPressed;
    private final BufferedImage avatarBluePressed;
    private final BufferedImage avatarPurplePressed;
    private final BufferedImage[] menuClouds;

    float[][] cloudPlacementsMenu;
    float[][] cloudPlacementsSettings;

    Color backgroundColor;
    Color pauseBgColor;

    // =========================================================================
    // FPS counter — updated once per second in debug mode
    // =========================================================================

    private long fpsTimer = System.nanoTime();
    private int frames = 0;
    private int fps = 0;


    // =========================================================================
    // Menu's Botton
    // =========================================================================

    // HOVER
    private final Map<ButtonValue.MainMenu, Boolean> mainMenuHover = new EnumMap<>(ButtonValue.MainMenu.class);
    private final Map<ButtonValue.Pause, Boolean> pauseHover = new EnumMap<>(ButtonValue.Pause.class);
    private final Map<ButtonValue.Settings, Boolean> settingsHover = new EnumMap<>(ButtonValue.Settings.class);
    private final Map<ButtonValue.GameOver, Boolean> gameOverHover = new EnumMap<>(ButtonValue.GameOver.class);

    // SELECTED
    private final Map<ButtonValue.MainMenu, Boolean> mainMenuSelected = new EnumMap<>(ButtonValue.MainMenu.class);
    private final Map<ButtonValue.Pause, Boolean> pauseSelected = new EnumMap<>(ButtonValue.Pause.class);
    private final Map<ButtonValue.Settings, Boolean> settingsSelected = new EnumMap<>(ButtonValue.Settings.class);
    private final Map<ButtonValue.GameOver, Boolean> gameOverSelected = new EnumMap<>(ButtonValue.GameOver.class);


    // =========================================================================
    // Damage flash state
    // =========================================================================

    private long damageFlashStartNano = -1L;
    private BufferedImage damageOverlayCache;
    private int damageOverlayCacheWidth = -1;
    private int damageOverlayCacheHeight = -1;
    private int damageOverlayAlphaStep = -1;

    /** Duration of the damage flash overlay in nanoseconds (0.5 s). */

    private static final int DAMAGE_ALPHA_STEPS = 24;
    private static final float[] DAMAGE_GRADIENT_DIST = { 0.0f, 0.60f, 0.82f, 1.0f };
    // =========================================================================
    // Shield state
    // =========================================================================
    private long shieldEffectStart = -1;
    private final double shieldDuration = EntityConfig.SHIELD_DURATION_MS;
    
    // =========================================================================
    // Win Screen & Particle State
    // =========================================================================
    private java.util.List<CoinParticle> coinParticles;
    // Internal class to handle single coin
    private static class CoinParticle {
        float x, y;
        float speed;
        float size;
        float angle;
        float rotationSpeed;

        public CoinParticle(int screenWidth, int screenHeight) {
            reset(screenWidth, screenHeight, true);
        }

        // Reset position of the coin
        public void reset(int screenWidth, int screenHeight, boolean randomInitialY) {
            this.x = (float) (Math.random() * screenWidth);
            this.y = randomInitialY ? (float) (Math.random() * - screenHeight) : -20; 
            this.speed = (float) (Math.random() * 3 + 2); // Fall speed
            this.size = (float) (Math.random() * 10 + 8);  // Coin's size
            this.angle = (float) (Math.random() * Math.PI * 2);
            this.rotationSpeed = (float) (Math.random() * 0.08 + 0.04); // Speed rotation
        }
    }


    // =========================================================================
    // Class Methods
    // =========================================================================

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public UI(GameModel gameModel, ScreenConfig screenConfig, int screenWidth, int screenHeight) {
        this.model = gameModel;
        this.screenConfig = screenConfig;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        maruMonica = loadFont("/res/fonts/x12y16pxMaruMonica.ttf");
        dungeonFont = loadFont("/res/fonts/DungeonFont.ttf");


        int tileSize = this.screenConfig.TILE_SIZE();
        heartFull = scaleImage(loadUiImage("src/res/UI/heart/heart_full.png"),  tileSize, tileSize);
        heartHalf = scaleImage(loadUiImage("src/res/UI/heart/heart_half.png"),  tileSize, tileSize);
        heartBlank = scaleImage(loadUiImage("src/res/UI/heart/heart_blank.png"), tileSize, tileSize);

        shield = loadUiImage("src/res/object/powerups/Icon_06.png");

        menuButton = new SliceSprite("src/res/UI/Buttons/Button_Blue_3Slides.png",  tileSize, tileSize);
        menuButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Hover_3Slides.png", tileSize, tileSize);
        ribbonBlueWide = new SliceSprite("src/res/UI/Ribbons/Ribbon_Blue_3Slides.png", tileSize, tileSize);
        pauseBanner = new SliceSprite("src/res/UI/Banners/Banner_Horizontal.png",tileSize,tileSize );
        dialogueBanner = new SliceSprite("src/res/UI/Banners/Banner_Horizontal.png", tileSize, tileSize);

        resumeButton = new SliceSprite("src/res/UI/Buttons/Button_Cyan_3Slides.png", tileSize, tileSize);
        resumeButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Cyan_3Slides_Pressed.png", tileSize, tileSize);
        saveButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Red_3Slides_Pressed.png", tileSize, tileSize);
        saveButton = new SliceSprite("src/res/UI/Buttons/Button_GrayRed_3Slides.png", tileSize, tileSize);

        menuLogo = loadUiImage("src/res/UI/Icons/logo_gioco.png");
        settingsIcon = scaleImage(loadUiImage("src/res/UI/Icons/Regular_11.png"), tileSize, tileSize);
        settingsIconPressed = scaleImage(loadUiImage("src/res/UI/Icons/Pressed_11.png"), tileSize, tileSize);

        avatarYellow = loadUiImage("src/res/UI/Human_Avatars/Avatar_Yellow.png");
        avatarRed = loadUiImage("src/res/UI/Human_Avatars/Avatar_Red.png");
        avatarBlue = loadUiImage("src/res/UI/Human_Avatars/Avatar_Blue.png");
        avatarPurple = loadUiImage("src/res/UI/Human_Avatars/Avatar_Purple.png");
        avatarYellowPressed = loadUiImage("src/res/UI/Human_Avatars/Avatar_Yellow_Selected.png");
        avatarRedPressed = loadUiImage("src/res/UI/Human_Avatars/Avatar_Red_Selected.png");
        avatarBluePressed = loadUiImage("src/res/UI/Human_Avatars/Avatar_Blue_Selected.png");
        avatarPurplePressed = loadUiImage("src/res/UI/Human_Avatars/Avatar_Purple_Selected.png");

        buttonMusic = loadUiImage("src/res/UI/Buttons/ButtonMusic.png");
        buttonMusicSelected = loadUiImage("src/res/UI/Buttons/ButtonMusic_Hover.png");
        buttonMusicPressed = loadUiImage("src/res/UI/Buttons/ButtonMusic_Pressed.png");
        buttonSound = loadUiImage("src/res/UI/Buttons/ButtonSound.png");
        buttonSoundSelected = loadUiImage("src/res/UI/Buttons/ButtonSound_Hover.png");
        buttonSoundPressed = loadUiImage("src/res/UI/Buttons/ButtonSound_Pressed.png");

        blueRibbon = new SliceSprite("src/res/UI/Ribbons/Ribbon_Blue_3Slides.png", tileSize, tileSize);
        redRibbon = new SliceSprite("src/res/UI/Ribbons/Ribbon_Red_3Slides.png", tileSize, tileSize);
        yellowRibbon = new SliceSprite("src/res/UI/Ribbons/Ribbon_Yellow_3Slides.png", tileSize, tileSize);


        menuClouds = new BufferedImage[] {
                loadUiImage("src/res/UI/Clouds/Clouds_01.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_02.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_03.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_04.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_05.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_06.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_07.png"),
                loadUiImage("src/res/UI/Clouds/Clouds_08.png")
        };

        // Each entry: { relativeX, relativeY, drawWidth }
        cloudPlacementsMenu = new float[][] {
                { 0.12f, 0.16f, 300f },
                { 0.28f, 0.72f, 300f },
                { 0.29f, 0.24f, 300f },
                { 0.11f, 0.49f, 300f },
                { 0.78f, 0.53f, 300f },
                { 0.89f, 0.88f, 300f },
                { 0.63f, 0.16f, 300f },
                { 0.82f, 0.26f, 300f }
        };
        cloudPlacementsSettings = new float[][] {
                { 0.10f, 0.10f, 200f },
                { 0.90f, 0.10f, 200f },
                { 0.21f, 0.19f, 600f },
        };

        backgroundColor = screenConfig.GAME_BG_COLOR();
        pauseBgColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 150);


        //initialize button state
        for (ButtonValue.MainMenu k : ButtonValue.MainMenu.values()) {
            mainMenuHover.put(k, false);
            mainMenuSelected.put(k, false);
        }
        for (ButtonValue.Pause k : ButtonValue.Pause.values()) {
            pauseHover.put(k, false);
            pauseSelected.put(k, false);
        }
        for (ButtonValue.Settings k : ButtonValue.Settings.values()) {
            settingsHover.put(k, false);
            settingsSelected.put(k, false);
        }
        for (ButtonValue.GameOver k : ButtonValue.GameOver.values()) {
            gameOverHover.put(k, false);
            gameOverSelected.put(k, false);
        }

    }
    //-------------------------------------------------------------


    /**
     * Main draw - called once per frame from the game loop.
     * */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        switch (model.getGameState()) {
            case MENU      -> {
                updateModelStatus();
                drawMainMenu();
            }
            case PLAYING   -> {
                drawPlayerLife();
                drawShield();
                if (!model.getCurrentDialogue().isEmpty()) drawDialogueWindow();
            }
            case PAUSED    -> {
                drawPlayerLife(); drawPauseScreen(); }
            case GAME_OVER -> drawGameOverScreen();
            case SETTINGS    -> {
                updateModelStatus();
                drawSettingsScreen();
            }
            case WIN -> drawWinScreen();
        }

        if (model.isDebugMode()) drawFpsOverlay();
    }

    /**
     * Sync the button state with the game model
     */
    //-------------------------------------------------------------
    private void updateModelStatus(){
        //Set start value based on model
        resetMainMenuSelected();
        resetSettingsSelected();

        switch (model.getPlayerColor()) {
            case YELLOW -> mainMenuSelected.put(ButtonValue.MainMenu.TOGGLE_YELLOW, true);
            case RED    -> mainMenuSelected.put(ButtonValue.MainMenu.TOGGLE_RED, true);
            case BLUE   -> mainMenuSelected.put(ButtonValue.MainMenu.TOGGLE_BLUE, true);
            case PURPLE -> mainMenuSelected.put(ButtonValue.MainMenu.TOGGLE_PURPLE, true);
        }
        switch (model.getFpsValue()) {
            case 0 -> settingsSelected.put(FPS_60, true);
            case 1 -> settingsSelected.put(FPS_120, true);
            case 2 -> settingsSelected.put(FPS_240, true);
        }

        switch (model.getResolutionValue()){
            case 0 -> settingsSelected.put(RES_MIN, true);
            case 1 -> settingsSelected.put(RES_MID, true);
            case 2 -> settingsSelected.put(RES_FULL, true);
        }

        if (!model.isMusicEnabled()){
            settingsSelected.put(ButtonValue.Settings.MUSIC, true);
        }
        if (!model.isSoundEnabled()){
            settingsSelected.put(ButtonValue.Settings.SOUND, true);
        }
    }
    //-------------------------------------------------------------


    // ALL DRAW METHODS
    //-------------------------------------------------------------
    private void drawPlayerLife() {

        int playerLife = model.getPlayer().getLife();
        int maxLife = model.getPlayer().getMaxLife();
        int totalHearts = (maxLife + 1) / 2;

        int heartWidth = heartFull.getWidth();
        int x = UIConfig.HUD_LIFE_X;
        int y = UIConfig.HUD_LIFE_Y;
        int spacing =  heartWidth / 6;

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

        // Draw the damage flash
        if (isDamageFlashActive()) {
            float elapsed = (System.nanoTime() - damageFlashStartNano) / (float) UIConfig.DAMAGE_FLASH_DURATION_NS;
            float alpha   = 1.0f - Math.min(1.0f, elapsed); // linear fade-out
            drawDamageOverlay(alpha);
        }
    }
    //-------------------------------------------------------------
    public void drawShield(){
        if (model.getPlayer().isShielded()){
            if (shieldEffectStart == -1) {
                shieldEffectStart = System.currentTimeMillis();
            }
            long passed = System.currentTimeMillis() - shieldEffectStart;
            float progress = 1.0f - (float)(passed/shieldDuration);
            if (progress<0) progress = 0;
            
            int barWidth = UIConfig.BAR_SHIELD_WIDTH;
            int barHeight = UIConfig.BAR_SHIELD_HEIGHT;
            int shieldSize = UIConfig.ICON_SHIELD_SIZE;
            int spacing = shieldSize / 6;

            int totalWidth = shieldSize + spacing + barWidth;
            int startX = screenWidth - totalWidth - UIConfig.SHIELD_OFFSET_SCREEN; 
            int startY = UIConfig.SHIELD_OFFSET_SCREEN; 

            Color originalColor = g2.getColor();
            Stroke originalStroke = g2.getStroke();

            int barX = startX + shieldSize + spacing;
            int barY = startY;
            // Bar background
            g2.setColor(new Color(35, 35, 35, 200));
            g2.fillRoundRect(barX, barY, barWidth, barHeight, 8, 8);

            // Moving bar
            g2.setColor(new Color(0, 190, 255));
            g2.fillRoundRect(barX, barY, (int) (barWidth * progress), barHeight, 8, 8);

            // Stroke of the bar
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(barX, barY, barWidth, barHeight, 8, 8);

            // Draw shield
            g2.drawImage(shield, startX, startY-((shieldSize-barHeight)/2), shieldSize, shieldSize, null);
            
            // Reset Graphics2D values
            g2.setColor(originalColor);
            g2.setStroke(originalStroke);

        } else {
            shieldEffectStart = -1;
        }

    }
    //-------------------------------------------------------------
    public void drawDialogueWindow() {

        int width = screenWidth- (screenConfig.TILE_SIZE() * 2);
        int height = screenConfig.TILE_SIZE() * 4;

        int x = (screenWidth - width) / 2;
        int y = screenHeight - height;

        dialogueBanner.draw(g2, x, y, width);

        String dialogue = model.getCurrentDialogue();
        if (dialogue == null || dialogue.isEmpty()) return;

        g2.setColor(new Color(60, 40, 20));
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 28F));

        int textX = x + 60;
        int textY = y + 80;
        int maxTextWidth = width - 120;

        for (String line : wrapText(dialogue, g2.getFontMetrics(), maxTextWidth)) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }

        g2.setFont(maruMonica.deriveFont(Font.ITALIC, 22F));
        g2.drawString("Press M to continue...", x + width - 300, y + height - 30);
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void drawMainMenu() {
        int w = screenWidth;
        int h = screenHeight;

        // backGround
        g2.setColor(new Color(140, 224, 228));
        g2.drawRect(0, 0, w - 1, h - 1);
        drawClouds(0, 0, w, h, cloudPlacementsMenu);

        //logo
        int logoWidth = UIConfig.MENU_LOGO_WIDTH;
        int logoHeight = (int) (((double) menuLogo.getHeight() / menuLogo.getWidth()) * logoWidth); //scale no distortion
        g2.drawImage(menuLogo, (w - logoWidth) / 2, 40, logoWidth, logoHeight, null);

        // Button
        MainMenuLayout layout = getMainMenuLayout();
        /*int selectedItem = mainMenuSelection;
        int hoveredRibbon = this.hoveredRibbon;
        int activeRibbon = this.activeRibbon;*/

        Rectangle newGameBounds = layout.newGameBounds();
        Rectangle continueBounds = layout.continueBounds();
        Rectangle settingsBounds = layout.settingsBounds();
        Rectangle ribbonYellowBounds = layout.toggleYellowBounds();
        Rectangle ribbonRedBounds = layout.toggleRedBounds();
        Rectangle ribbonBlueBounds = layout.toggleBlueBounds();
        Rectangle ribbonPurpleBounds = layout.togglePurpleBounds();

        //draw ribbon
        g2.drawImage(mainMenuHover.get(ButtonValue.MainMenu.TOGGLE_BLUE) || mainMenuSelected.get(ButtonValue.MainMenu.TOGGLE_BLUE)
                        ? avatarBluePressed : avatarBlue,
                ribbonBlueBounds.x, ribbonBlueBounds.y, ribbonBlueBounds.width, ribbonBlueBounds.height, null);

        g2.drawImage(mainMenuHover.get(TOGGLE_YELLOW) || mainMenuSelected.get(TOGGLE_YELLOW)
                        ? avatarYellowPressed : avatarYellow,
                ribbonYellowBounds.x, ribbonYellowBounds.y, ribbonYellowBounds.width, ribbonYellowBounds.height, null);

        g2.drawImage(mainMenuHover.get(TOGGLE_RED) || mainMenuSelected.get(TOGGLE_RED)
                        ? avatarRedPressed : avatarRed,
                ribbonRedBounds.x, ribbonRedBounds.y, ribbonRedBounds.width, ribbonRedBounds.height, null);

        g2.drawImage(mainMenuHover.get(TOGGLE_PURPLE) || mainMenuSelected.get(TOGGLE_PURPLE)
                        ? avatarPurplePressed : avatarPurple,
                ribbonPurpleBounds.x, ribbonPurpleBounds.y, ribbonPurpleBounds.width, ribbonPurpleBounds.height, null);

        //draw button
        drawButton(menuButton, menuButtonSelected, newGameBounds.x,  newGameBounds.y,  newGameBounds.width,  newGameBounds.height,
                "New Game", mainMenuHover.get(NEW_GAME)  || mainMenuSelected.get(NEW_GAME));
        drawButton(menuButton, menuButtonSelected, continueBounds.x, continueBounds.y, continueBounds.width, continueBounds.height,
                "Resume",   mainMenuHover.get(LOAD_GAME)  || mainMenuSelected.get(LOAD_GAME));

        //draw settings icon
        g2.drawImage(mainMenuHover.get(SETTINGS) || mainMenuSelected.get(SETTINGS)
                        ? settingsIconPressed : settingsIcon,
                settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);
    }
    //-------------------------------------------------------------
    private void drawClouds(int panelX, int panelY, int panelW, int panelH, float[][] cloudPlacements) {
        if (menuClouds == null || menuClouds.length == 0) return;

        for (int i = 0; i < cloudPlacements.length; i++) {

            BufferedImage cloud = menuClouds[i % menuClouds.length]; //get clouds from array circularly

            int drawWidth  = Math.round(cloudPlacements[i][2]); //when you define a placement you set a width for it
            int drawHeight = Math.round(drawWidth * ((float) cloud.getHeight() / cloud.getWidth())); //correctly scale

            int drawX = panelX + Math.round(cloudPlacements[i][0] * panelW) - drawWidth  / 2;
            int drawY = panelY + Math.round(cloudPlacements[i][1] * panelH) - drawHeight / 2;
            g2.drawImage(cloud, drawX, drawY, drawWidth, drawHeight, null);
        }

    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void drawPauseScreen() {

        g2.setColor(pauseBgColor);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        PauseMenuLayout layout = getPauseMenuLayout();
        Rectangle ribbonBounds = layout.pauseRibbonBounds();
        Rectangle resumeBounds = layout.resumeBounds();
        Rectangle settingsBounds = layout.settingsBounds();
        Rectangle saveBounds = layout.saveBounds();

        // Pause text
        pauseBanner.draw(g2, ribbonBounds.x, ribbonBounds.y, ribbonBounds.width);
        String title = "PAUSE";
        g2.setColor(new Color(60, 40, 20, 200));
        drawTextInRibbon(ribbonBounds, title, 1, 1);

        drawButton(resumeButton, resumeButtonSelected, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height,
                "Resume", pauseHover.get(RESUME) || pauseSelected.get(RESUME));

        drawButton(saveButton, saveButtonSelected, saveBounds.x, saveBounds.y, saveBounds.width, saveBounds.height,
                "Save & Exit", pauseHover.get(SAVE) || pauseSelected.get(SAVE));

        g2.drawImage(pauseHover.get(PAUSE_SETTINGS) || pauseSelected.get(PAUSE_SETTINGS) ? settingsIconPressed : settingsIcon,
                settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);

    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void drawSettingsScreen() {

        SettingsLayout layout = getSettingsLayout();

        //background
        Rectangle sb = layout.settingsBounds();
        g2.setColor(new Color(209, 205, 180));
        g2.fillRoundRect(sb.x, sb.y, sb.width, sb.height, 20, 20);
        g2.setColor(new Color(30, 30, 80));
        g2.setStroke(new BasicStroke(7));
        g2.drawRoundRect(sb.x, sb.y, sb.width, sb.height, 20, 20);
        g2.setStroke(new BasicStroke(1)); // reset stroke
        // -------------------------------------------------------

        // Settings icon
        Rectangle settingsBounds = layout.settingsIconBounds();
        g2.drawImage(settingsHover.get(SETTINGS_ICON) || settingsSelected.get(SETTINGS_ICON)
                        ? settingsIconPressed : settingsIcon,
                settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);

        drawClouds(sb.x, sb.y, sb.width, sb.height, cloudPlacementsSettings);
        // -------------------------------------------------------

        //Audio section
        Rectangle audioRibbon = layout.audioRibbonBounds();
        redRibbon.draw(g2, audioRibbon.x, audioRibbon.y, audioRibbon.width, audioRibbon.height);

        String title = "Audio Settings";
        g2.setColor(Color.WHITE);
        drawTextInRibbon(audioRibbon, title, 0.5 , 0.9);

        Rectangle musicB = layout.musicBounds();
        if(settingsSelected.get(MUSIC)){
            g2.drawImage(buttonMusicPressed, musicB.x, musicB.y, musicB.width, musicB.height, null);
        }else{
            g2.drawImage(settingsHover.get(MUSIC) ? buttonMusicSelected : buttonMusic,
                    musicB.x, musicB.y, musicB.width, musicB.height, null);}


        Rectangle soundB = layout.soundBounds();
        if(settingsSelected.get(SOUND)){
            g2.drawImage(buttonSoundPressed, soundB.x, soundB.y, soundB.width, soundB.height, null);
        }else{
            g2.drawImage(settingsHover.get(SOUND) ? buttonSoundSelected : buttonSound,
                    soundB.x, soundB.y, soundB.width, soundB.height, null);
        }
        // -------------------------------------------------------

        //Resolution
        Rectangle resRibbon = layout.resRibbonBounds();
        blueRibbon.draw(g2, resRibbon.x, resRibbon.y, resRibbon.width, resRibbon.height);

        title = "Screen Resolution";
        drawTextInRibbon(resRibbon, title, 0.5, 0.97);

        drawButton(menuButton, menuButtonSelected,
                layout.resFullBounds().x, layout.resFullBounds().y,
                layout.resFullBounds().width, layout.resFullBounds().height,
                "FULL", settingsHover.get(RES_FULL) || settingsSelected.get(RES_FULL));

        drawButton(menuButton, menuButtonSelected,
                layout.resHalfBounds().x, layout.resHalfBounds().y,
                layout.resHalfBounds().width, layout.resHalfBounds().height,
                "MID", settingsHover.get(RES_MID) || settingsSelected.get(RES_MID));

        drawButton(menuButton, menuButtonSelected,
                layout.resMinBounds().x, layout.resMinBounds().y,
                layout.resMinBounds().width, layout.resMinBounds().height,
                "SMAL", settingsHover.get(RES_MIN) || settingsSelected.get(RES_MIN));

        // -------------------------------------------------------

        // Fps
        Rectangle fpsRibbon = layout.fpsRibbonBounds();
        yellowRibbon.draw(g2, fpsRibbon.x, fpsRibbon.y, fpsRibbon.width, fpsRibbon.height);

        title = "FPS";
        drawTextInRibbon(fpsRibbon, title, 0.5, 0.98);

        drawButton(menuButton, menuButtonSelected,
                layout.fpsBounds1().x, layout.fpsBounds1().y,
                layout.fpsBounds1().width, layout.fpsBounds1().height,
                "60", settingsHover.get(FPS_60) || settingsSelected.get(FPS_60));

        drawButton(menuButton, menuButtonSelected,
                layout.fpsBounds2().x, layout.fpsBounds2().y,
                layout.fpsBounds2().width, layout.fpsBounds2().height,
                "120", settingsHover.get(FPS_120) || settingsSelected.get(FPS_120));

        drawButton(menuButton, menuButtonSelected,
                layout.fpsBounds3().x, layout.fpsBounds3().y,
                layout.fpsBounds3().width, layout.fpsBounds3().height,
                "240", settingsHover.get(FPS_240) || settingsSelected.get(FPS_240));
        // -------------------------------------------------------
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void drawGameOverScreen() {

        // Dark semi-transparent overlay
        g2.setColor(new Color(8, 8, 8, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        GameOverLayout layout = getGameOverLayout();
        Rectangle ribbonBounds = layout.gameOverRibbonBounds();
        Rectangle newGameBounds = layout.newGameBounds();

        // Pause text
        redRibbon.draw(g2, ribbonBounds.x, ribbonBounds.y, ribbonBounds.width, ribbonBounds.height);
        String title = "GAME OVER";
        g2.setColor(Color.white);
        drawTextInRibbon(ribbonBounds, title, 0.6, 0.9);

        // Restart button
        drawButton(resumeButton, resumeButtonSelected, newGameBounds.x, newGameBounds.y, newGameBounds.width, newGameBounds.height,
                "Main Menu", gameOverHover.get(RESTART) || gameOverSelected.get(RESTART));
    }
    //-------------------------------------------------------------
    private void drawWinScreen(){
        if (coinParticles == null){
            coinParticles = new java.util.ArrayList<>();
            for (int i=0; i<60; i++){
                coinParticles.add(new CoinParticle(screenWidth, screenHeight));
            }
        }

        drawPlayerLife();

        // Overlay
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g2.setColor(new Color(25, 20, 5));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setComposite(oldComposite);

        // Rain of coins
        g2.setColor(new Color (255, 215, 0));
        for (CoinParticle coin: coinParticles){
            coin.y += coin.speed;
            coin.angle += coin.rotationSpeed; // Spin the coin

            // If it leaves the bottom of the screen, it respawns at the top
            if (coin.y > screenHeight) {
                coin.reset (screenWidth, screenHeight, false);
            }

            // Simulate 3D rotation by changing the width of the coin using the cosine of the angle
            int animatedWidth = (int) (coin.size * Math.abs(Math.cos(coin.angle)));
            int animatedHeight = (int) coin.size;

            // Calculate position
            int drawX = (int) (coin.x + (coin.size - animatedWidth) / 2);
            int drawY = (int) coin.y;

            // Draw the shadow of the coin
            g2.setColor(new Color(150, 100, 0, 100));
            g2.fillOval(drawX+ 2, drawY + 2, animatedWidth, animatedHeight);

            // Draw the coin
            g2.setColor(new Color(255, 215, 0));
            g2.fillOval(drawX, drawY, animatedWidth, animatedHeight);

            // Internal edge for depth
            g2.setColor(new Color(200, 140, 0));
            g2.drawOval(drawX, drawY, animatedWidth, animatedHeight);
        }

        // Title of win
        int titleRibbonWidth = 520;
        int titleRibbonHeight = 80;
        int titleRibbonX = (screenWidth - titleRibbonWidth) / 2;
        int titleRibbonY = 72;
        yellowRibbon.draw(g2, titleRibbonX, titleRibbonY, titleRibbonWidth, titleRibbonHeight);
        

        // Text
        g2.setColor(new Color(60, 40, 20));
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 70F));
        String title = "YOU WIN!";
        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(title, g2);
        int textX = getXforCenteredText(title);
        int textY = titleRibbonY + (int) Math.round((titleRibbonHeight - textBounds.getHeight()) / 2.0 - textBounds.getY());
        g2.drawString(title, textX, textY);

        // Button for newgame
        GameOverLayout layout = getGameOverLayout();
        Rectangle newGameBounds = layout.newGameBounds();
    
        // Disegnamo il pulsante usando i tuoi asset standard
        drawButton(menuButton, menuButtonSelected, newGameBounds.x, newGameBounds.y, newGameBounds.width, newGameBounds.height,
                "Play Again", gameOverHover.get(RESTART) || gameOverSelected.get(RESTART));
    }
    //-------------------------------------------------------------

    /**
     * Draws a menu button sprite with a horizontally and vertically centred label. */
    //-------------------------------------------------------------
    private void drawButton(SliceSprite menuButton, SliceSprite menuButtonSelected, int x, int y, int width, int height, String label, boolean selected) {
        (selected ? menuButtonSelected : menuButton).draw(g2, x, y, width, height);

        g2.setColor(Color.WHITE);
        Font fittedFont = fitFontToBox(g2, label, maruMonica.deriveFont(Font.BOLD, UIConfig.MAX_BUTTON_TEXT_SIZE),
                (int) (width*0.7) , (int) (height * 0.5));
        g2.setFont(fittedFont);

        int constant = 2;

        if (selected & menuButtonSelected != this.menuButtonSelected) {
            constant = 5;
        }

        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(label, g2);
        int textX = x + (int) Math.round((width - textBounds.getWidth()) / 2.0 - textBounds.getX());
        int textY = constant + y + (int) Math.round((height - textBounds.getHeight()) / 2.0 - textBounds.getY());

        g2.drawString(label, textX, textY);

    }
    //-------------------------------------------------------------
    private void drawTextInRibbon(Rectangle ribbonBounds, String title, double ribbonScale, double constY) {

        Font fittedFont = fitFontToBox(g2, title, maruMonica.deriveFont(Font.BOLD, UIConfig.MAX_RIBBON_TEXT_SIZE),
                (int)(ribbonBounds.width * ribbonScale), (int)(ribbonBounds.height * ribbonScale) );
        g2.setFont(fittedFont);

        Rectangle2D fontBounds = g2.getFontMetrics().getStringBounds(title, g2);
        int textX = ribbonBounds.x + (int) Math.round((ribbonBounds.width - fontBounds.getWidth()) / 2.0 - fontBounds.getX());
        int textY = ribbonBounds.y + (int) Math.round((ribbonBounds.height - fontBounds.getHeight()) / 2.0 - fontBounds.getY());
        g2.drawString(title, textX, (int)(textY*constY));
    }
    //-------------------------------------------------------------



    // Damage overlay
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private boolean isDamageFlashActive() {
        return damageFlashStartNano >= 0
                && (System.nanoTime() - damageFlashStartNano) < UIConfig.DAMAGE_FLASH_DURATION_NS;
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void drawDamageOverlay(float alpha) {
        int w = screenWidth;
        int h = screenHeight;

        int alphaStep = Math.clamp(Math.round(alpha * DAMAGE_ALPHA_STEPS), 0, DAMAGE_ALPHA_STEPS);
        if (damageOverlayCache == null
                || damageOverlayCacheWidth != w
                || damageOverlayCacheHeight != h
                || damageOverlayAlphaStep != alphaStep) {
            rebuildDamageOverlayCache(w, h, alphaStep / (float) DAMAGE_ALPHA_STEPS);
        }
        g2.drawImage(damageOverlayCache, 0, 0, null);
    }
    //-------------------------------------------------------------
    /**
     * The class redraw all theOverlay sprites in the game only when need
     * to improve rendering performance.
     */
    private void rebuildDamageOverlayCache(int w, int h, float alpha) {
        damageOverlayCache = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        damageOverlayCacheWidth = w;
        damageOverlayCacheHeight = h;
        damageOverlayAlphaStep = Math.round(alpha * DAMAGE_ALPHA_STEPS);

        Graphics2D g = damageOverlayCache.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final Color warmFlashColor = new Color(255, 220, 220);
        final Color borderColor = new Color(190, 20, 20);
        final BasicStroke borderStroke = new BasicStroke(10f);

        long startNs = System.nanoTime();
        // 1) Subtle full-screen warm flash
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f * alpha));
        g.setColor(warmFlashColor);
        g.fillRect(0, 0, w, h);

        // 2) Radial red vignette: transparent at centre, opaque at edges
        float      radius = Math.max(w, h) * 0.62f;
        Point2D    center = new Point2D.Float(w / 2f, h / 2f);
        Color[]    colors = {
                new Color(0,   0, 0, 0),
                new Color(120, 0, 0, 0),
                new Color(180, 0, 0, (int)(90  * alpha)),
                new Color(120, 0, 0, (int)(190 * alpha))
        };
        g.setPaint(new RadialGradientPaint(center, radius, DAMAGE_GRADIENT_DIST, colors));
        g.setComposite(AlphaComposite.SrcOver);
        g.fillRect(0, 0, w, h);

        // 3) Thin red inner border
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * alpha));
        g.setStroke(borderStroke);
        g.setColor(borderColor);
        g.drawRect(0, 0, w, h);

        g.dispose();
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
        if (model.isDebugMode() && elapsedMs >= 4) {
            System.out.println("[UI] damage overlay cache rebuilt in " + elapsedMs + "ms (alphaStep="
                    + damageOverlayAlphaStep + ", " + w + "x" + h + ")");
        }
    }
    //-------------------------------------------------------------


    //LAYOUT
    //-------------------------------------------------------------
    public MainMenuLayout getMainMenuLayout() {

        int buttonWidth = UIConfig.MENU_BUTTON_WIDTH;
        int buttonHeight = UIConfig.MENU_BUTTON_HEIGHT;
        int centerX = screenWidth / 2;
        int firstY = (screenHeight / 2) + screenConfig.TILE_SIZE();
        int gap = UIConfig.MENU_PADDING;

        Rectangle newGameBounds  = new Rectangle(centerX - buttonWidth / 2, firstY, buttonWidth, buttonHeight);
        Rectangle continueBounds = new Rectangle(centerX - buttonWidth / 2, firstY + buttonHeight + gap, buttonWidth, buttonHeight);

        int settingsSize = UIConfig.MENU_BUTTON_SETTINGS_SIZE;
        Rectangle settingsBounds = new Rectangle(screenWidth - settingsSize - UIConfig.MENU_PADDING,
                UIConfig.MENU_PADDING,
                settingsSize,
                settingsSize);

        int ribbonX = UIConfig.MENU_RIBBON_X;
        int ribbonY = UIConfig.MENU_RIBBON_Y;
        int ribbonW = UIConfig.MENU_RIBBON_SIZE;
        int ribbonH = UIConfig.MENU_RIBBON_SIZE;

        Rectangle ribbonBlueBounds = new Rectangle(ribbonX, ribbonY, ribbonW, ribbonH);
        Rectangle ribbonYellowBounds = new Rectangle(ribbonX, ribbonY + ribbonH, ribbonW, ribbonH);
        Rectangle ribbonRedBounds = new Rectangle(ribbonX, ribbonY + ribbonH*2,ribbonW, ribbonH);
        Rectangle ribbonPurpleBounds = new Rectangle(ribbonX, ribbonY + ribbonH*3,ribbonW, ribbonH);

        return new MainMenuLayout(newGameBounds, continueBounds, settingsBounds,
                ribbonYellowBounds, ribbonRedBounds, ribbonBlueBounds, ribbonPurpleBounds);
    }
    //-------------------------------------------------------------
    public GameOverLayout getGameOverLayout() {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        //banner
        int ribbonW = (int) (screenWidth * 0.55f);
        int ribbonH = UIConfig.GAME_OVER_RIBBON_HEIGHT;
        int ribbonX =  (screenWidth - ribbonW) / 2;

        int ribbonY =  UIConfig.GAME_OVER_PADDING;
        Rectangle gameOverRibbonBounds = new Rectangle(ribbonX, ribbonY, ribbonW, ribbonH);


        //button
        int newGameButtonWidth  = UIConfig.RESUME_BUTTON_WIDTH;
        int newGameButtonHeight = UIConfig.RESUME_BUTTON_HEIGHT;
        int gap = UIConfig.GAME_OVER_PADDING ;
        int firstButtonY = screenHeight - newGameButtonHeight - gap;
        Rectangle newGameBounds = new Rectangle(centerX - newGameButtonWidth / 2, firstButtonY, newGameButtonWidth, newGameButtonHeight);

        return new GameOverLayout(newGameBounds, gameOverRibbonBounds);
    }
    //-------------------------------------------------------------
    public PauseMenuLayout getPauseMenuLayout() {

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int bannerWidth  = UIConfig.BANNER_WIDTH;
        int bannerHeight = pauseBanner.getImageHeight();
        int bannerX = centerX - bannerWidth / 2;
        int bannerY = centerY - bannerHeight / 2 - UIConfig.PAUSE_RIBBON_OFFSET_Y;

        Rectangle pauseRibbonBounds = new Rectangle(bannerX, bannerY, bannerWidth, bannerHeight);

        int resumButtonWidth  = UIConfig.RESUME_BUTTON_WIDTH;
        int resumeButtonHeight = UIConfig.RESUME_BUTTON_HEIGHT;
        int saveButtonWidth  = UIConfig.SAVE_BUTTON_WIDTH;
        int saveButtonHeight = UIConfig.SAVE_BUTTON_HEIGHT;
        int gap = UIConfig.PAUSE_PADDING;
        int firstButtonY = bannerY + bannerHeight + gap;

        Rectangle resumeBounds = new Rectangle(centerX - resumButtonWidth / 2, firstButtonY, resumButtonWidth, resumeButtonHeight);
        Rectangle saveBounds   = new Rectangle(centerX - saveButtonWidth / 2, firstButtonY + saveButtonHeight + gap, saveButtonWidth, saveButtonHeight);

        int settingsSize = UIConfig.MENU_BUTTON_SETTINGS_SIZE;
        Rectangle settingsBounds = new Rectangle(screenWidth - settingsSize - UIConfig.MENU_PADDING,
                UIConfig.MENU_PADDING,
                settingsSize,
                settingsSize);

        return new PauseMenuLayout(resumeBounds, settingsBounds, saveBounds, pauseRibbonBounds);
    }
    //-------------------------------------------------------------
    public SettingsLayout getSettingsLayout() {

        int sw = screenWidth;
        int sh = screenHeight;

        //bg
        int settingsW = (int) (sw * 0.98f);
        int settingsH = (int) (sh * 0.98f);
        int settingsX = (sw - settingsW) / 2;
        int settingsY = (sh - settingsH) / 2;
        Rectangle settingsBounds = new Rectangle(settingsX, settingsY, settingsW, settingsH);

        // settings icon
        int settingsSize = UIConfig.MENU_BUTTON_SETTINGS_SIZE;
        Rectangle settingsIconBounds = new Rectangle(
                sw - settingsSize - UIConfig.MENU_PADDING,
                UIConfig.MENU_PADDING,
                settingsSize,
                settingsSize
        );

        // audio rubbon
        int ribbonW = (int) (settingsW * 0.55f);
        int ribbonH = UIConfig.SETTINGS_RIBBON_HEIGHT;
        int ribbonX = settingsX + (settingsW - ribbonW) / 2;

        int audioRibbonY = settingsY + UIConfig.SETTINGS_PADDING *3;
        Rectangle audioRibbonBounds = new Rectangle(ribbonX, audioRibbonY, ribbonW, ribbonH);


        // audio icon
        int iconSize = UIConfig.SETTINGS_ICON_SIZE;
        int iconsY = audioRibbonY + ribbonH + UIConfig.SETTINGS_PADDING;
        int totalIconsW = iconSize * 2 + UIConfig.SETTINGS_PADDING;
        int iconsStartX = settingsX + (settingsW - totalIconsW) / 2;

        Rectangle musicBounds = new Rectangle(iconsStartX, iconsY, iconSize, iconSize);
        Rectangle soundBounds = new Rectangle(iconsStartX + iconSize + UIConfig.SETTINGS_PADDING, iconsY, iconSize, iconSize);

        // screen ribbon
        int screenRibbonY = iconsY + iconSize + UIConfig.SETTINGS_PADDING * 2;
        Rectangle resRibbonBounds = new Rectangle(ribbonX, screenRibbonY, ribbonW, ribbonH);

        // screen button
        int btnW = UIConfig.SETTINGS_BUTTON_WIDTH;
        int btnH = UIConfig.SETTINGS_BUTTON_HEIGHT;
        int btnY = screenRibbonY + ribbonH + UIConfig.SETTINGS_PADDING;

        int totalBtnsW = btnW * 3 + UIConfig.SETTINGS_PADDING * 2;
        int btnsStartX = settingsX + (settingsW - totalBtnsW) / 2;

        Rectangle resFullBounds = new Rectangle(btnsStartX, btnY, btnW, btnH);
        Rectangle resHalfBounds = new Rectangle(btnsStartX + btnW + UIConfig.SETTINGS_PADDING, btnY, btnW, btnH);
        Rectangle resMinBounds = new Rectangle(btnsStartX + (btnW + UIConfig.SETTINGS_PADDING) * 2, btnY, btnW, btnH);

        // fps ribbon
        int fpsRibbonY = btnY + btnH + UIConfig.SETTINGS_PADDING * 2;
        Rectangle fpsRibbonBounds = new Rectangle(ribbonX, fpsRibbonY, ribbonW, ribbonH);

        // fps button
        int fpsBtnY = fpsRibbonY + ribbonH + UIConfig.SETTINGS_PADDING;
        Rectangle fpsBounds1 = new Rectangle(btnsStartX, fpsBtnY, btnW, btnH);
        Rectangle fpsBounds2 = new Rectangle(btnsStartX + btnW + UIConfig.SETTINGS_PADDING, fpsBtnY, btnW, btnH);
        Rectangle fpsBounds3 = new Rectangle(btnsStartX + (btnW + UIConfig.SETTINGS_PADDING) * 2, fpsBtnY, btnW, btnH);

        return new SettingsLayout(
                settingsBounds,
                settingsIconBounds,
                audioRibbonBounds,
                musicBounds,
                soundBounds,
                fpsRibbonBounds,
                fpsBounds1,
                fpsBounds2,
                fpsBounds3,
                resRibbonBounds,
                resFullBounds,
                resHalfBounds,
                resMinBounds
        );

    }
    //-------------------------------------------------------------


    // DEBUG METHODS
    //-------------------------------------------------------------
    private void drawFpsOverlay() {
        frames++;
        long now = System.nanoTime();
        if (now - fpsTimer >= 1_000_000_000L) {
            fps      = frames;
            frames   = 0;
            fpsTimer = now;
        }

        Player player = model.getPlayer();
        int xTile = (player.getWorldX() + player.getSolidArea().x) / screenConfig.TILE_SIZE();
        int yTile = (player.getWorldY() + player.getSolidArea().y) / screenConfig.TILE_SIZE();

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("FPS: " + fps
                + "  PLAYER X: " + xTile + ", Y: " + yTile
                + "  L: " + player.getCurrentLayer(), 10, 18);
    }
    //-------------------------------------------------------------


    // CLASS UTILITY METHODS
    /**
     * Returns the X coordinate to center horizontally the text on screen. */
    //-------------------------------------------------------------
    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return screenWidth / 2 - length / 2;
    }

    //-------------------------------------------------------------
    /**
     *  Returns a new BufferedImage that is a scaled copy of original. */
    //-------------------------------------------------------------
    public BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, original.getType());
        Graphics2D    g      = scaled.createGraphics();
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }
    //-------------------------------------------------------------
    /**
     * Loads an image from the given file-system path, throwing on failure. */
    //-------------------------------------------------------------
    private BufferedImage loadUiImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load UI image: " + path, e);
        }
    }
    //-------------------------------------------------------------
    /**
     * Loads a TrueType font from the given classpath resource, throwing on failure. */
    //-------------------------------------------------------------
    private Font loadFont(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException("Unable to load font: " + resourcePath, e);
        }
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String paragraph : text.split("\n")) {

            String[] words = paragraph.split(" ");
            String line = "";

            for (String word : words) {
                String testLine = line.isEmpty() ? word : line + " " + word;

                if (fm.stringWidth(testLine) <= maxWidth) {
                    line = testLine;
                } else {
                    if (!line.isEmpty()) {
                        lines.add(line);
                    }
                    line = word;
                }
            }

            if (!line.isEmpty()) {
                lines.add(line);
            }
        }

        return lines;
    }
    //-------------------------------------------------------------
    private Font fitFontToBox(Graphics2D g2, String text, Font baseFont, int maxWidth, int maxHeight) {
        int size = baseFont.getSize();

        while (size > UIConfig.MIN_BUTTON_TEXT_SIZE) {
            Font testFont = baseFont.deriveFont((float) size);
            FontMetrics fm = g2.getFontMetrics(testFont);

            if (fm.stringWidth(text) <= maxWidth && fm.getHeight() <= maxHeight) {
                return testFont;
            }

            size--;
        }

        return baseFont;
    }
    //-------------------------------------------------------------


    // SETTER
    //-------------------------------------------------------------
    public void setScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void triggerDamageFlash() {
        // Starts a 0.5-second red damage flash overlay.
        damageFlashStartNano = System.nanoTime();
    }

    // MainMenu
    public void setMainMenuHover(ButtonValue.MainMenu key) {
        mainMenuHover.replaceAll((k, v) -> key != null && k == key);
    }
    public void setMainMenuSelected(ButtonValue.MainMenu key) {
        mainMenuSelected.replaceAll((k, v) -> key != null && k == key);
    }
    public void resetMainMenuHover() {
        mainMenuHover.replaceAll((k, v) -> false);
    }
    public void resetMainMenuSelected() {
        mainMenuSelected.replaceAll((k, v) -> false);
    }

    // Pause
    public void setPauseHover(ButtonValue.Pause key) {
        pauseHover.replaceAll((k, v) -> key != null && k == key);
    }
    public void setPauseSelected(ButtonValue.Pause key) {
        pauseSelected.replaceAll((k, v) -> key != null && k == key);
    }
    public void resetPauseHover() {
        pauseHover.replaceAll((k, v) -> false);
    }
    public void resetPauseSelected() {
        pauseSelected.replaceAll((k, v) -> false);
    }

    // Settings
    public void setSettingsHover(ButtonValue.Settings key) {
        settingsHover.replaceAll((k, v) -> key != null && k == key);
    }
    public void setSettingsSelected(ButtonValue.Settings key) {
        settingsSelected.replaceAll((k, v) -> key != null && k == key);
    }
    public void resetSettingsHover() {
        settingsHover.replaceAll((k, v) -> false);
    }
    public void resetSettingsSelected() {
        settingsSelected.replaceAll((k, v) -> false);
    }

    // GameOver
    public void setGameOverHover(ButtonValue.GameOver key) {
        gameOverHover.replaceAll((k, v) -> key != null && k == key);
    }
    public void setGameOverSelected(ButtonValue.GameOver key) {
        gameOverSelected.replaceAll((k, v) -> key != null && k == key);
    }
    public void resetGameOverHover() {
        gameOverHover.replaceAll((k, v) -> false);
    }
    public void resetGameOverSelected() {
        gameOverSelected.replaceAll((k, v) -> false);
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
// end class UI