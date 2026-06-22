package tinyswordsisland.view.ui;

import tinyswordsisland.config.enu.ButtonValue;
import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.ScreenConfig;
import tinyswordsisland.config.UIConfig;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import tinyswordsisland.view.GameViewState;

import static tinyswordsisland.config.enu.ButtonValue.MainMenu.*;
import static tinyswordsisland.config.enu.ButtonValue.PauseMenu.*;
import static tinyswordsisland.config.enu.ButtonValue.SettingsMenu.*;
import static tinyswordsisland.config.enu.ButtonValue.GameOverMenu.*;
import static tinyswordsisland.config.enu.ButtonValue.WinMenu.HOME_WIN;
import static tinyswordsisland.config.enu.ButtonValue.WinMenu.QUIT_WIN;


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
    private Graphics2D g2;
    private GameViewState viewState;

    // =========================================================================
    // Font
    // =========================================================================

    private final Font maruMonica;
    // =========================================================================
    // Assets — loaded once at construction time
    // =========================================================================

    private final BufferedImage heartFull;
    private final BufferedImage heartHalf;
    private final BufferedImage heartBlank;

    private final BufferedImage shield;

    private final SliceSprite goldButton;
    private final SliceSprite goldButtonSelected;
    private final SliceSprite pauseBanner;
    private final SliceSprite dialogueBanner;
    private final SliceSprite blueBanner;

    private final SliceSprite blueButton;
    private final SliceSprite blueButtonSelected;
    private final SliceSprite redButton;
    private final SliceSprite redButtonSelected;

    private final SliceSprite grayButton;

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

    private final Map<Enum<?>, Boolean> hoverState  = new HashMap<>();
    private final Map<Enum<?>, Boolean> selectedState = new HashMap<>();

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
            this.size = (float) (Math.random() * 10 + 14);  // Coin's size
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
    public UI(ScreenConfig screenConfig, int screenWidth, int screenHeight) {
        this.screenConfig = screenConfig;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        maruMonica = loadFont("/res/fonts/x12y16pxMaruMonica.ttf");

        int tileSize = this.screenConfig.TILE_SIZE();
        heartFull = scaleImage(loadUiImage("src/res/UI/heart/heart_full.png"),  tileSize, tileSize);
        heartHalf = scaleImage(loadUiImage("src/res/UI/heart/heart_half.png"),  tileSize, tileSize);
        heartBlank = scaleImage(loadUiImage("src/res/UI/heart/heart_blank.png"), tileSize, tileSize);

        shield = loadUiImage("src/res/object/powerups/Icon_06.png");

        goldButton = new SliceSprite("src/res/UI/Buttons/Button_Blue_3Slides.png",  tileSize, tileSize);
        goldButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Hover_3Slides.png", tileSize, tileSize);
        pauseBanner = new SliceSprite("src/res/UI/Banners/Banner_Horizontal.png",tileSize,tileSize );
        dialogueBanner = new SliceSprite("src/res/UI/Banners/Banner_Horizontal.png", tileSize, tileSize);
        blueBanner = new SliceSprite("src/res/UI/Banners/sword_Banner.png", tileSize*2, tileSize);

        blueButton = new SliceSprite("src/res/UI/Buttons/Button_Cyan_3Slides.png", tileSize, tileSize);
        blueButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Cyan_3Slides_Pressed.png", tileSize, tileSize);
        redButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Red_3Slides_Pressed.png", tileSize, tileSize);
        redButton = new SliceSprite("src/res/UI/Buttons/Button_GrayRed_3Slides.png", tileSize, tileSize);

        grayButton = new SliceSprite("src/res/UI/Buttons/Button_Gray_3Slides.png", tileSize, tileSize);

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
        for (ButtonValue.MainMenu k  : ButtonValue.MainMenu.values())  { 
            hoverState.put(k, false); 
            selectedState.put(k, false); 
        } for (ButtonValue.PauseMenu k : ButtonValue.PauseMenu.values()) { 
            hoverState.put(k, false); 
            selectedState.put(k, false); 
        } for (ButtonValue.SettingsMenu k : ButtonValue.SettingsMenu.values()) { 
            hoverState.put(k, false); 
            selectedState.put(k, false); 
        } for (ButtonValue.GameOverMenu k : ButtonValue.GameOverMenu.values()) { 
            hoverState.put(k, false); 
            selectedState.put(k, false); 
        } for (ButtonValue.WinMenu k : ButtonValue.WinMenu.values()) { 
            hoverState.put(k, false); 
            selectedState.put(k, false); 
        }
    }
    //-------------------------------------------------------------


    /**
     * Main draw - called once per frame from the game loop.
     * */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2, GameViewState state) {
        this.g2 = g2;
        this.viewState = state;

        switch (state.gameState()) {
            case MENU      -> {
                updateModelStatus();
                drawMainMenu();
            }
            case PLAYING   -> {
                drawPlayerLife();
                drawShield();
                if (!state.currentDialogue().isEmpty()) drawDialogueWindow();
                if (!state.currentMessage().isEmpty()) drawMessageWindow();
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

        if (state.debugMode()) drawFpsOverlay();
    }

    /**
     * Sync the button state with the game tinyswordsisland.model
     */
    //-------------------------------------------------------------
    private void updateModelStatus(){
        // reset selected for MainMenu
        ButtonValue.MainMenu[] mainItems = ButtonValue.MainMenu.values();
        for (ButtonValue.MainMenu k : mainItems) selectedState.put(k, false);

        // reset selected for SettingsMenu
        ButtonValue.SettingsMenu[] settingsItems = ButtonValue.SettingsMenu.values();
        for (ButtonValue.SettingsMenu k : settingsItems) selectedState.put(k, false);

        switch (viewState.playerColor()) {
            case YELLOW -> selectedState.put(ButtonValue.MainMenu.TOGGLE_YELLOW, true);
            case RED    -> selectedState.put(ButtonValue.MainMenu.TOGGLE_RED, true);
            case BLUE   -> selectedState.put(ButtonValue.MainMenu.TOGGLE_BLUE, true);
            case PURPLE -> selectedState.put(ButtonValue.MainMenu.TOGGLE_PURPLE, true);
        }

        switch (viewState.resolutionValue()) {
            case 0 -> selectedState.put(RES_MIN, true);
            case 1 -> selectedState.put(RES_MID, true);
            case 2 -> selectedState.put(RES_FULL, true);
        }

        if (!viewState.musicEnabled()) {
            selectedState.put(ButtonValue.SettingsMenu.MUSIC, true);
        }
        if (!viewState.soundEnabled()) {
            selectedState.put(ButtonValue.SettingsMenu.SOUND, true);
        }
    }
    //-------------------------------------------------------------


    // ALL DRAW METHODS
    //-------------------------------------------------------------
    private void drawPlayerLife() {

        int playerLife = viewState.playerLife();
        int maxLife = viewState.playerMaxLife();
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
    private void drawShield() {
        if (!viewState.playerHasShield()) {
            return;
        }

        double remainingMs = viewState.playerShieldTimerMs();
        double maxMs = EntityConfig.SHIELD_DURATION_MS;

        float progress = (float) (remainingMs / maxMs);
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;

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

        // Remaining shield bar
        g2.setColor(new Color(0, 190, 255));
        g2.fillRoundRect(barX, barY, (int) (barWidth * progress), barHeight, 8, 8);

        // Bar border
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(barX, barY, barWidth, barHeight, 8, 8);

        // Shield icon
        g2.drawImage(shield, startX, startY - ((shieldSize - barHeight) / 2), shieldSize, shieldSize, null);

        g2.setColor(originalColor);
        g2.setStroke(originalStroke);
    }
    //-------------------------------------------------------------
    private void drawDialogueWindow() {

        int width = (int) (screenWidth * UIConfig.DIALOUE_WIDTH_PCT);
        int height = (int) (screenHeight * UIConfig.DIALOUE_HEIGHT_PCT);

        int x = (screenWidth - width) / 2;
        int y = screenHeight - height - (int) (screenHeight * UIConfig.DIALOUE_PADDING_PCT);

        dialogueBanner.draw(g2, x, y, width, height);

        String dialogue = viewState.currentDialogue();
        if (dialogue == null || dialogue.isBlank()) return;

        int textBoxWidth = (int) (width * 0.85f);
        int textBoxHeight = (int) (height * 0.35f);

        Rectangle textBounds = new Rectangle(
                x + (width - textBoxWidth) / 2,
                y + (height -  textBoxHeight)/ 2,
                textBoxWidth,
                textBoxHeight
        );


        drawWrappedText(
                textBounds,
                dialogue,
                maruMonica.deriveFont(Font.BOLD, UIConfig.MAX_DIALOGUE_TEXT_SIZE),
                new Color(60, 40, 20)
        );
    }

    private void drawMessageWindow(){
        int width = (int) (screenWidth * UIConfig.MESSAGE_WIDTH_PCT);
        int height =  blueBanner.getImageHeight();

        int x = (screenWidth - width) / 2;
        int y = (int) (screenHeight * UIConfig.MESSAGE_PADDING_PCT);

        blueBanner.draw(g2, x, y, width, height);


        String allert = viewState.currentMessage();
        if (allert == null || allert.isBlank()) return;

        int textBoxWidth = (int) (width * 0.75f);
        int textBoxHeight = (int) (height * 0.5f);


        Rectangle textBounds = new Rectangle(
                x + (width - textBoxWidth + 40) / 2,
                y + (height -  textBoxHeight)/ 2,
                textBoxWidth,
                textBoxHeight
        );
        g2.setColor(Color.RED);;


        drawWrappedText(
                textBounds,
                allert,
                maruMonica.deriveFont(Font.BOLD, UIConfig.MAX_DIALOGUE_TEXT_SIZE),
                new Color(66, 80, 98)
        );
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



        // Button
        MainMenuLayout layout = getMainMenuLayout();
        /*int selectedItem = mainMenuSelection;
        int hoveredRibbon = this.hoveredRibbon;
        int activeRibbon = this.activeRibbon;*/

        Rectangle logoBounds = layout.logoBounds();
        Rectangle newGameBounds = layout.newGameBounds();
        Rectangle continueBounds = layout.continueBounds();
        Rectangle settingsBounds = layout.settingsBounds();
        Rectangle ribbonYellowBounds = layout.toggleYellowBounds();
        Rectangle ribbonRedBounds = layout.toggleRedBounds();
        Rectangle ribbonBlueBounds = layout.toggleBlueBounds();
        Rectangle ribbonPurpleBounds = layout.togglePurpleBounds();


        g2.drawImage(menuLogo, logoBounds.x, logoBounds.y, logoBounds.width, logoBounds.height, null);

        //draw ribbon
        g2.drawImage(hoverState.get(ButtonValue.MainMenu.TOGGLE_BLUE) || selectedState.get(ButtonValue.MainMenu.TOGGLE_BLUE)
                        ? avatarBluePressed : avatarBlue,
                ribbonBlueBounds.x, ribbonBlueBounds.y, ribbonBlueBounds.width, ribbonBlueBounds.height, null);

        g2.drawImage(hoverState.get(TOGGLE_YELLOW) || selectedState.get(TOGGLE_YELLOW)
                        ? avatarYellowPressed : avatarYellow,
                ribbonYellowBounds.x, ribbonYellowBounds.y, ribbonYellowBounds.width, ribbonYellowBounds.height, null);

        g2.drawImage(hoverState.get(TOGGLE_RED) || selectedState.get(TOGGLE_RED)
                        ? avatarRedPressed : avatarRed,
                ribbonRedBounds.x, ribbonRedBounds.y, ribbonRedBounds.width, ribbonRedBounds.height, null);

        g2.drawImage(hoverState.get(TOGGLE_PURPLE) || selectedState.get(TOGGLE_PURPLE)
                        ? avatarPurplePressed : avatarPurple,
                ribbonPurpleBounds.x, ribbonPurpleBounds.y, ribbonPurpleBounds.width, ribbonPurpleBounds.height, null);

        //draw button
        drawButton(goldButton, goldButtonSelected, newGameBounds.x,  newGameBounds.y,  newGameBounds.width,  newGameBounds.height,
                "New Game", hoverState.get(NEW_GAME)  || selectedState.get(NEW_GAME));
        drawButton(goldButton, goldButtonSelected, continueBounds.x, continueBounds.y, continueBounds.width, continueBounds.height,
                "Resume",   hoverState.get(LOAD_GAME)  || selectedState.get(LOAD_GAME));

        //draw settings icon
        g2.drawImage(hoverState.get(SETTINGS) || selectedState.get(SETTINGS)
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
    private void drawCoinRain(Graphics2D g2, int currentWidth, int currentHeight) {

        final double BASE_WIDTH = 1152; // TODO no magic numbers
        double scale = currentWidth / BASE_WIDTH;

        for (CoinParticle coin : coinParticles) {

            double scaledSpeed = coin.speed * scale;
            coin.y += scaledSpeed;
            coin.angle += coin.rotationSpeed;

            // coin reset out of screen
            if (coin.y > currentHeight) {
                coin.reset(currentWidth, currentHeight, false);
            }

            double scaledSize = coin.size * scale;

            int animatedWidth = (int) (scaledSize * Math.abs(Math.cos(coin.angle)));
            int animatedHeight = (int) scaledSize;

            int drawX = (int) (coin.x + (scaledSize - animatedWidth) / 2);
            int drawY = (int) coin.y;

            // shadow
            int shadowOffset = Math.max(1, (int)(2 * scale));
            g2.setColor(new Color(150, 100, 0, 100));
            g2.fillOval(drawX + shadowOffset, drawY + shadowOffset, animatedWidth, animatedHeight);

            // Coin
            g2.setColor(new Color(255, 215, 0));
            g2.fillOval(drawX, drawY, animatedWidth, animatedHeight);

            // border
            g2.setColor(new Color(200, 140, 0));
            g2.drawOval(drawX, drawY, animatedWidth, animatedHeight);
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

        // PauseMenu text
        pauseBanner.draw(g2, ribbonBounds.x, ribbonBounds.y, ribbonBounds.width, ribbonBounds.height);
        String title = "PAUSE";
        g2.setColor(new Color(60, 40, 20, 200));
        drawTextInRibbon(ribbonBounds, title, 0.5, 1);

        drawButton(blueButton, blueButtonSelected, resumeBounds.x, resumeBounds.y, resumeBounds.width, resumeBounds.height,
                "Resume", hoverState.get(RESUME) || selectedState.get(RESUME));

        drawButton(redButton, redButtonSelected, saveBounds.x, saveBounds.y, saveBounds.width, saveBounds.height,
                "Save & Exit", hoverState.get(SAVE) || selectedState.get(SAVE));

        g2.drawImage(hoverState.get(PAUSE_SETTINGS) || selectedState.get(PAUSE_SETTINGS) ? settingsIconPressed : settingsIcon,
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

        // SettingsMenu icon
        Rectangle settingsBounds = layout.settingsIconBounds();
        g2.drawImage(hoverState.get(SETTINGS_ICON) || selectedState.get(SETTINGS_ICON)
                        ? settingsIconPressed : settingsIcon,
                settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);

        drawClouds(sb.x, sb.y, sb.width, sb.height, cloudPlacementsSettings);
        // -------------------------------------------------------

        //Audio section
        Rectangle audioRibbon = layout.audioRibbonBounds();
        yellowRibbon.draw(g2, audioRibbon.x, audioRibbon.y, audioRibbon.width, audioRibbon.height);

        String title = "Audio Settings";
        g2.setColor(Color.WHITE);
        drawTextInRibbon(audioRibbon, title, 0.5 , 0.9);

        Rectangle musicB = layout.musicBounds();
        if(selectedState.get(MUSIC)){
            g2.drawImage(buttonMusicPressed, musicB.x, musicB.y, musicB.width, musicB.height, null);
        }else{
            g2.drawImage(hoverState.get(MUSIC) ? buttonMusicSelected : buttonMusic,
                    musicB.x, musicB.y, musicB.width, musicB.height, null);}


        Rectangle soundB = layout.soundBounds();
        if(selectedState.get(SOUND)){
            g2.drawImage(buttonSoundPressed, soundB.x, soundB.y, soundB.width, soundB.height, null);
        }else{
            g2.drawImage(hoverState.get(SOUND) ? buttonSoundSelected : buttonSound,
                    soundB.x, soundB.y, soundB.width, soundB.height, null);
        }
        // -------------------------------------------------------

        //Resolution
        Rectangle resRibbon = layout.resRibbonBounds();
        blueRibbon.draw(g2, resRibbon.x, resRibbon.y, resRibbon.width, resRibbon.height);

        title = "Screen Resolution";
        drawTextInRibbon(resRibbon, title, 0.5, 0.97);

        drawButton(goldButton, goldButtonSelected,
                layout.resFullBounds().x, layout.resFullBounds().y,
                layout.resFullBounds().width, layout.resFullBounds().height,
                "Full Screen", hoverState.get(RES_FULL) || selectedState.get(RES_FULL));

        drawButton(goldButton, goldButtonSelected,
                layout.resHalfBounds().x, layout.resHalfBounds().y,
                layout.resHalfBounds().width, layout.resHalfBounds().height,
                "Full Window", hoverState.get(RES_MID) || selectedState.get(RES_MID));

        drawButton(goldButton, goldButtonSelected,
                layout.resMinBounds().x, layout.resMinBounds().y,
                layout.resMinBounds().width, layout.resMinBounds().height,
                "Small Window", hoverState.get(RES_MIN) || selectedState.get(RES_MIN));

        // -------------------------------------------------------

        // Quit

        drawButton(grayButton, redButtonSelected,
                layout.quitBounds().x, layout.quitBounds().y,
                layout.quitBounds().width, layout.quitBounds().height,
                "QUIT", hoverState.get(QUIT) || selectedState.get(QUIT));

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
        Rectangle homeButtonBounds = layout.homeButtonBounds();
        Rectangle quitButtonBounds = layout.quitButtonBounds();

        // PauseMenu text
        redRibbon.draw(g2, ribbonBounds.x, ribbonBounds.y, ribbonBounds.width, ribbonBounds.height);
        String title = "GAME OVER";
        g2.setColor(Color.white);
        drawTextInRibbon(ribbonBounds, title, 0.6, 0.9);

        // home button
        drawButton(blueButton, blueButtonSelected, homeButtonBounds.x, homeButtonBounds.y, homeButtonBounds.width, homeButtonBounds.height,
                "Main Menu", hoverState.get(HOME_OVER) || selectedState.get(HOME_OVER));

        // quit button
        drawButton(grayButton, redButtonSelected, quitButtonBounds.x, quitButtonBounds.y, quitButtonBounds.width, quitButtonBounds.height,
                "Quit", hoverState.get(QUIT_OVER) || selectedState.get(QUIT_OVER));
    }
    //-------------------------------------------------------------
    private void drawWinScreen() {

        if (coinParticles == null) {
            coinParticles = new java.util.ArrayList<>();
            for (int i = 0; i < 60; i++) {
                coinParticles.add(new CoinParticle(screenWidth, screenHeight));
            }
        }

        //bg
        g2.setColor(new Color(8, 8, 8, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        //coin
        drawCoinRain(g2, screenWidth, screenHeight);

        WinLayout layout = getWinLayout();
        Rectangle ribbonBounds = layout.winRibbonBounds();
        Rectangle homeButtonBounds = layout.homeButtonBounds();
        Rectangle quitButtonBounds = layout.quitButtonBounds();

        // PauseMenu text
        yellowRibbon.draw(g2, ribbonBounds.x, ribbonBounds.y, ribbonBounds.width, ribbonBounds.height);
        String title = "YOU WIN";
        g2.setColor(Color.white);
        drawTextInRibbon(ribbonBounds, title, 0.6, 0.97);

        // home button
        drawButton(blueButton, blueButtonSelected, homeButtonBounds.x, homeButtonBounds.y, homeButtonBounds.width, homeButtonBounds.height,
                "Main Menu", hoverState.get(HOME_WIN) || selectedState.get(HOME_WIN));

        // quit button
        drawButton(grayButton, redButtonSelected, quitButtonBounds.x, quitButtonBounds.y, quitButtonBounds.width, quitButtonBounds.height,
                "Quit", hoverState.get(QUIT_WIN) || selectedState.get(QUIT_WIN));
    }
    //-------------------------------------------------------------

    /**
     * Draws a menu button sprite with a horizontally and vertically centred label. */
    //-------------------------------------------------------------
    private void drawButton(SliceSprite menuButton, SliceSprite menuButtonSelected, int x, int y, int width, int height, String label, boolean selected) {
        (selected ? menuButtonSelected : menuButton).draw(g2, x, y, width, height);

        g2.setColor(Color.WHITE);
        Font fittedFont = fitFontToBox(g2, label, maruMonica.deriveFont(Font.BOLD, UIConfig.MAX_BUTTON_TEXT_SIZE),
                (int) (width*UIConfig.BUTTON_INSIDE_PADDING_W) , (int) (height * UIConfig.BUTTON_INSIDE_PADDING_H));
        g2.setFont(fittedFont);

        int constant = 2;

        if (selected & menuButtonSelected != this.goldButtonSelected) {
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
        if (viewState.debugMode() && elapsedMs >= 4) {
            System.out.println("[UI] damage overlay cache rebuilt in " + elapsedMs + "ms (alphaStep="
                    + damageOverlayAlphaStep + ", " + w + "x" + h + ")");
        }
    }
    //-------------------------------------------------------------


    //LAYOUT
    //-------------------------------------------------------------
    public MainMenuLayout getMainMenuLayout() {

        int minDim = Math.min(screenWidth, screenHeight);

        int buttonWidth  = (int) (screenWidth  * UIConfig.MENU_BUTTON_WIDTH_PCT);
        int buttonHeight = (int) (screenHeight * UIConfig.MENU_BUTTON_HEIGHT_PCT);
        int centerX = screenWidth / 2;

        int gap = (int) (screenHeight * UIConfig.MENU_PADDING_PCT);

        //logo
        int logoWidth = (int) (screenWidth * UIConfig.MENU_LOGO_WIDTH);
        int logoHeight = (int) (((double) menuLogo.getHeight() / menuLogo.getWidth()) * logoWidth); //scale no distortion
        int logoX = centerX - logoWidth / 2;

        int firstY = logoHeight + gap*6;

        Rectangle logoBounds = new Rectangle(logoX, gap, logoWidth, logoHeight);
        Rectangle newGameBounds  = new Rectangle(centerX - buttonWidth / 2, firstY, buttonWidth, buttonHeight);
        Rectangle continueBounds = new Rectangle(centerX - buttonWidth / 2, firstY + buttonHeight + gap, buttonWidth, buttonHeight);

        int settingsSize = (int) (minDim * UIConfig.MENU_BUTTON_SETTINGS_SIZE_PCT);
        Rectangle settingsBounds = new Rectangle(screenWidth - settingsSize - gap*2,
                gap*2,
                settingsSize,
                settingsSize);

        int ribbonX = (int) (screenWidth  * UIConfig.MENU_PADDING_PCT);
        int ribbonY = (int) (screenHeight * UIConfig.MENU_PADDING_PCT);
        int ribbonW = (int) (minDim * UIConfig.MENU_RIBBON_SIZE_PCT);
        int ribbonH = ribbonW;

        Rectangle ribbonBlueBounds   = new Rectangle(ribbonX, ribbonY, ribbonW, ribbonH);
        Rectangle ribbonYellowBounds = new Rectangle(ribbonX, ribbonY + ribbonH, ribbonW, ribbonH);
        Rectangle ribbonRedBounds    = new Rectangle(ribbonX, ribbonY + ribbonH * 2, ribbonW, ribbonH);
        Rectangle ribbonPurpleBounds = new Rectangle(ribbonX, ribbonY + ribbonH * 3, ribbonW, ribbonH);

        return new MainMenuLayout(logoBounds, newGameBounds, continueBounds, settingsBounds,
                ribbonYellowBounds, ribbonRedBounds, ribbonBlueBounds, ribbonPurpleBounds);
    }
    //-------------------------------------------------------------
    public GameOverLayout getGameOverLayout() {
        int centerX = screenWidth / 2;

        //banner
        int ribbonW = (int) (screenWidth  * UIConfig.GAME_OVER_RIBBON_WIDTH_PCT);
        int ribbonH = (int) (screenHeight * UIConfig.GAME_OVER_RIBBON_HEIGHT_PCT);
        int ribbonX = (screenWidth - ribbonW) / 2;
        int ribbonY = (int) (screenHeight * UIConfig.GAME_OVER_PADDING_PCT);

        Rectangle gameOverRibbonBounds = new Rectangle(ribbonX, ribbonY, ribbonW, ribbonH);

        //button
        int buttonWidth  = (int) (screenWidth  * UIConfig.GAME_OVER_BUTTON_WIDTH_PCT);
        int buttonHeight = (int) (screenHeight * UIConfig.GAME_OVER_BUTTON_HEIGHT_PCT);
        int gap = (int) (screenHeight * UIConfig.GAME_OVER_PADDING_PCT);

        int buttonY = screenHeight - buttonHeight - gap;
        int buttonX = centerX - buttonWidth - gap / 2;

        Rectangle homeButtonBounds = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
        buttonX = centerX + gap / 2;
        Rectangle quitButtonBounds = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        return new GameOverLayout(gameOverRibbonBounds, homeButtonBounds, quitButtonBounds);
    }
    //-------------------------------------------------------------

    public WinLayout getWinLayout() {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        //banner
        int ribbonW = (int) (screenWidth  * UIConfig.WIN_RIBBON_WIDTH_PCT);
        int ribbonH = (int) (screenHeight * UIConfig.WIN_RIBBON_HEIGHT_PCT);
        int ribbonX = (screenWidth - ribbonW) / 2;
        int ribbonY = centerY - ribbonH;

        Rectangle winRibbonBounds = new Rectangle(ribbonX, ribbonY, ribbonW, ribbonH);

        //button
        int buttonWidth  = (int) (screenWidth  * UIConfig.WIN_BUTTON_WIDTH_PCT);
        int buttonHeight = (int) (screenHeight * UIConfig.WIN_BUTTON_HEIGHT_PCT);
        int gap = (int) (screenHeight * UIConfig.WIN_PADDING_PCT);

        int buttonY = screenHeight - buttonHeight - gap;
        int buttonX = centerX - buttonWidth - gap / 2;

        Rectangle homeButtonBounds = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
        buttonX = centerX + gap / 2;
        Rectangle quitButtonBounds = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

        return new WinLayout(winRibbonBounds, homeButtonBounds, quitButtonBounds);



    }
    //-------------------------------------------------------------

    public PauseMenuLayout getPauseMenuLayout() {

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int bannerWidth = (int) (screenWidth * UIConfig.BANNER_WIDTH_PCT);
        int bannerHeight = bannerWidth / UIConfig.BANNER_ASPECT_RATIO;

        int bannerX = centerX - bannerWidth / 2;
        int bannerY = centerY - bannerHeight / 2  ;

        Rectangle pauseRibbonBounds = new Rectangle(bannerX, bannerY, bannerWidth, bannerHeight);


        int buttonWidth    = (int) (screenWidth  * UIConfig.PAUSE_BUTTON_WIDTH_PCT);
        int buttonHeight   = (int) (screenHeight * UIConfig.PAUSE_BUTTON_HEIGHT_PCT);
        int gap = (int) (screenHeight * UIConfig.PAUSE_PADDING_PCT);
        int firstButtonY = bannerY + bannerHeight + gap *4;
        int firstButtonX = centerX - buttonWidth/2;

        Rectangle resumeBounds = new Rectangle(firstButtonX, firstButtonY, buttonWidth, buttonHeight);
        Rectangle saveBounds   = new Rectangle(firstButtonX, firstButtonY + buttonHeight + gap, buttonWidth, buttonHeight);

        int minDim = Math.min(screenWidth, screenHeight);
        int settingsSize = (int) (minDim * UIConfig.MENU_BUTTON_SETTINGS_SIZE_PCT);
        int menuPadding  = (int) (screenHeight * UIConfig.MENU_PADDING_PCT);
        Rectangle settingsBounds = new Rectangle(screenWidth - settingsSize - menuPadding,
                menuPadding,
                settingsSize,
                settingsSize);

        return new PauseMenuLayout(resumeBounds, settingsBounds, saveBounds, pauseRibbonBounds);
    }
    //-------------------------------------------------------------
    public SettingsLayout getSettingsLayout() {

        int sw = screenWidth;
        int sh = screenHeight;
        int minDim = Math.min(sw, sh);

        //bg
        int settingsW = (int) (sw * 0.98f);
        int settingsH = (int) (sh * 0.98f);
        int settingsX = (sw - settingsW) / 2;
        int settingsY = (sh - settingsH) / 2;
        Rectangle settingsBounds = new Rectangle(settingsX, settingsY, settingsW, settingsH);

        // settings icon
        int menuPadding = (int) (sh * UIConfig.MENU_PADDING_PCT);
        int settingsSize = (int) (minDim * UIConfig.MENU_BUTTON_SETTINGS_SIZE_PCT);
        Rectangle settingsIconBounds = new Rectangle(
                sw - settingsSize - menuPadding*2,
                menuPadding*2,
                settingsSize,
                settingsSize
        );

        // audio ribbon
        int settingsPadding = (int) (sh * UIConfig.SETTINGS_PADDING_PCT);
        int ribbonW = (int) (settingsW * 0.55f);
        int ribbonH = (int) (sh * UIConfig.SETTINGS_RIBBON_HEIGHT_PCT);
        int ribbonX = settingsX + (settingsW - ribbonW) / 2;

        int audioRibbonY = settingsY + settingsPadding * 3;
        Rectangle audioRibbonBounds = new Rectangle(ribbonX, audioRibbonY, ribbonW, ribbonH);

        // audio icon
        int iconSize = (int) (minDim * UIConfig.SETTINGS_ICON_SIZE_PCT);
        int iconsY = audioRibbonY + ribbonH + settingsPadding;
        int totalIconsW = iconSize * 2 + settingsPadding;
        int iconsStartX = settingsX + (settingsW - totalIconsW) / 2;

        Rectangle musicBounds = new Rectangle(iconsStartX, iconsY, iconSize, iconSize);
        Rectangle soundBounds = new Rectangle(iconsStartX + iconSize + settingsPadding, iconsY, iconSize, iconSize);

        // screen ribbon
        int screenRibbonY = iconsY + iconSize + settingsPadding * 2;
        Rectangle resRibbonBounds = new Rectangle(ribbonX, screenRibbonY, ribbonW, ribbonH);

        // screen button
        int btnW = (int) (sw * UIConfig.SETTINGS_BUTTON_WIDTH_PCT);
        int btnH = (int) (sh * UIConfig.SETTINGS_BUTTON_HEIGHT_PCT);
        int btnY = screenRibbonY + ribbonH + settingsPadding;

        int totalBtnsW = btnW * 3 + settingsPadding * 2;
        int btnsStartX = settingsX + (settingsW - totalBtnsW) / 2;

        Rectangle resFullBounds = new Rectangle(btnsStartX, btnY, btnW, btnH);
        Rectangle resHalfBounds = new Rectangle(btnsStartX + btnW + settingsPadding, btnY, btnW, btnH);
        Rectangle resMinBounds  = new Rectangle(btnsStartX + (btnW + settingsPadding) * 2, btnY, btnW, btnH);

        btnH *= 1.6;
        // quit button
        btnsStartX = settingsX + (settingsW - btnW) / 2;
        int quitBtnY = btnY + btnH + settingsPadding * 8;
        Rectangle quitBounds = new Rectangle(btnsStartX, quitBtnY, btnW, btnH);

        return new SettingsLayout(
                settingsBounds,
                settingsIconBounds,
                audioRibbonBounds,
                musicBounds,
                soundBounds,
                quitBounds,
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

        int xTile = (viewState.playerWorldX() + viewState.playerSolidArea().x) / screenConfig.TILE_SIZE();
        int yTile = (viewState.playerWorldY() + viewState.playerSolidArea().y) / screenConfig.TILE_SIZE();

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("FPS: " + fps
                + "  PLAYER X: " + xTile + ", Y: " + yTile
                + "  L: " + viewState.playerCurrentLayer(), 10, 18);
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
    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
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

        while (size >= UIConfig.MIN_BUTTON_TEXT_SIZE) {
            Font testFont = baseFont.deriveFont((float) size);
            FontMetrics fm = g2.getFontMetrics(testFont);

            java.util.List<String> lines = wrapText(text, fm, maxWidth);

            int totalHeight = lines.size() * fm.getHeight();
            int maxLineWidth = 0;

            for (String line : lines) {
                maxLineWidth = Math.max(maxLineWidth, fm.stringWidth(line));
            }

            if (maxLineWidth <= maxWidth && totalHeight <= maxHeight) {
                return testFont;
            }

            size--;
        }

        return baseFont.deriveFont((float) UIConfig.MIN_BUTTON_TEXT_SIZE);
    }
    //-------------------------------------------------------------

    private void drawWrappedText(Rectangle bounds, String text, Font baseFont, Color color) {
        if (text == null || text.isBlank()) return;

        int fontSize = Math.max(UIConfig.MIN_BUTTON_TEXT_SIZE, (int)(bounds.height * 0.45));

        Font fittedFont = baseFont.deriveFont((float) fontSize);
        g2.setFont(fittedFont);
        g2.setColor(color);

        FontMetrics fm = g2.getFontMetrics();
        java.util.List<String> lines = wrapText(text, fm, bounds.width);

        int x = bounds.x;
        int y = bounds.y + fm.getAscent();
        int lineHeight = fm.getHeight();

        for (String line : lines) {
            g2.drawString(line, x, y);
            y += lineHeight;
        }
    }


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
    public void setHover(Enum<?> key) {
    hoverState.replaceAll((k, v) -> k.getClass() == key.getClass() && k == key);
    }
    public void resetHover(Class<? extends Enum<?>> menuClass) {
        hoverState.replaceAll((k, v) -> k.getClass() != menuClass && v);
    }
    public void setSelected(Enum<?> key) {
        selectedState.replaceAll((k, v) -> k.getClass() == key.getClass() && k == key);
    }

}
//-------------------------------------------------------------------------------------------------------------------
// end class UI