package view.UI;

import main.CONFIG.enu.ButtonValue;
import model.GameModel;
import model.entity.Player;
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

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final ScreenConfig screenConfig;
    private final GameModel gameModel;
    private Graphics2D g2;

    // =========================================================================
    // Fonts
    // =========================================================================

    private final Font maruMonica;

    // =========================================================================
    // Assets — loaded once at construction time
    // =========================================================================

    private final BufferedImage heartFull;
    private final BufferedImage heartHalf;
    private final BufferedImage heartBlank;

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
    private final BufferedImage buttonSound;
    private final BufferedImage buttonSoundSelected;

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
    // Menu's
    // =========================================================================


   /* private int mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
    private int hoveredRibbon = UIConfig.RIBBON_DEFAULT;
    private int activeRibbon = UIConfig.RIBBON_DEFAULT;
    private boolean hoveredGameOverButton = false;
    private int pauseMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;

    private int settingsSelection = -1;*/


    // =========================================================================
    // Damage flash state
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

    private final Map<ButtonValue.MainMenu, Boolean> ribbonSelected = new EnumMap<>(ButtonValue.MainMenu.class);
    private final Map<ButtonValue.Settings, Boolean> musicSelected = new EnumMap<>(ButtonValue.Settings.class);
    private final Map<ButtonValue.Settings, Boolean> soundSelected = new EnumMap<>(ButtonValue.Settings.class);
    private final Map<ButtonValue.Settings, Boolean> fpsSeclected = new EnumMap<>(ButtonValue.Settings.class);
    private final Map<ButtonValue.Settings, Boolean> screenSeclected = new EnumMap<>(ButtonValue.Settings.class);



    private long damageFlashStartNano = -1L;
    private BufferedImage damageOverlayCache;
    private int damageOverlayCacheWidth = -1;
    private int damageOverlayCacheHeight = -1;
    private int damageOverlayAlphaStep = -1;

    /** Duration of the damage flash overlay in nanoseconds (0.5 s). */

    private static final int DAMAGE_ALPHA_STEPS = 24;
    private static final float[] DAMAGE_GRADIENT_DIST = { 0.0f, 0.60f, 0.82f, 1.0f };

    // =========================================================================
    // Class Methods
    // =========================================================================

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public UI(GameModel gameModel, ScreenConfig screenConfig) {
        this.gameModel = gameModel;
        this.screenConfig = screenConfig;

        maruMonica = loadFont("/res/fonts/x12y16pxMaruMonica.ttf");

        int tileSize = this.screenConfig.TILE_SIZE();
        heartFull = scaleImage(loadUiImage("src/res/UI/heart/heart_full.png"),  tileSize, tileSize);
        heartHalf = scaleImage(loadUiImage("src/res/UI/heart/heart_half.png"),  tileSize, tileSize);
        heartBlank = scaleImage(loadUiImage("src/res/UI/heart/heart_blank.png"), tileSize, tileSize);

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
        buttonMusicSelected = loadUiImage("src/res/UI/Buttons/ButtonMusic_Pressed.png");
        buttonSound = loadUiImage("src/res/UI/Buttons/ButtonSound.png");
        buttonSoundSelected = loadUiImage("src/res/UI/Buttons/ButtonSound_Pressed.png");

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
        // ribbonSelected: solo le quattro chiavi ribbon
        ribbonSelected.put(TOGGLE_BLUE,   true);
        ribbonSelected.put(TOGGLE_YELLOW, false);
        ribbonSelected.put(TOGGLE_RED,    false);
        ribbonSelected.put(TOGGLE_PURPLE, false);

        musicSelected.put(MUSIC, false);
        soundSelected.put(SOUND, false);
        fpsSeclected.put(FPS_120, true);
        fpsSeclected.put(FPS_60,  false);
        fpsSeclected.put(FPS_240, false);
        screenSeclected.put(RES_MID, true);
        screenSeclected.put(RES_FULL, false);
        screenSeclected.put(RES_MIN,  false);




    }
    //-------------------------------------------------------------


    /**
     * Main draw - called once per frame from the game loop.
     * */
    //-------------------------------------------------------------
    public void draw(Graphics2D g2) {
        this.g2 = g2;

        switch (gameModel.getGameState()) {
            case MENU      -> drawMainMenu();
            case PLAYING   -> {

                drawPlayerLife();
                if (!gameModel.getCurrentDialogue().isEmpty()) drawDialogueWindow();
            }
            case PAUSED    -> { drawPlayerLife(); drawPauseScreen(); }
            case GAME_OVER -> drawGameOverScreen();
            case SETTINGS    -> drawSettingsScreen();
        }

        if (gameModel.isDebugMode()) drawFpsOverlay();
    }
    //-------------------------------------------------------------
    // ALL DRAW METHODS
    //-------------------------------------------------------------
    private void drawPlayerLife() {

        int playerLife = gameModel.getPlayer().getLife();
        int maxLife = gameModel.getPlayer().getMaxLife();
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
    public void drawDialogueWindow() {

        int width = screenConfig.SCREEN_WIDTH() - (screenConfig.TILE_SIZE() * 2);
        int height = screenConfig.TILE_SIZE() * 4;

        int x = (screenConfig.SCREEN_WIDTH() - width) / 2;
        int y = screenConfig.SCREEN_HEIGHT() - height;

        dialogueBanner.draw(g2, x, y, width);

        String dialogue = gameModel.getCurrentDialogue();
        if (dialogue == null || dialogue.isEmpty()) return;

        // Dialogue text
        g2.setColor(new Color(60, 40, 20));
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 28F));

        //TODO far andare a capo il testo in automatioc
        int textX = x + 60;
        int textY = y + 80;
        for (String line : dialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }

        // "Press M to continue" hint
        g2.setFont(maruMonica.deriveFont(Font.ITALIC, 22F));
        g2.drawString("Press M to continue...", x + width - 300, y + height - 130);
    }
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void drawMainMenu() {
        int w = screenConfig.SCREEN_WIDTH();
        int h = screenConfig.SCREEN_HEIGHT();

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
        Rectangle ribbonYellowBounds = layout.ribbonYellowBounds();
        Rectangle ribbonRedBounds = layout.ribbonRedBounds();
        Rectangle ribbonBlueBounds = layout.ribbonBlueBounds();
        Rectangle ribbonPurpleBounds = layout.ribbonPurpleBounds();

        //draw ribbon
        g2.drawImage(mainMenuHover.get(ButtonValue.MainMenu.TOGGLE_BLUE) || ribbonSelected.get(ButtonValue.MainMenu.TOGGLE_BLUE)
                        ? avatarBluePressed : avatarBlue,
                ribbonBlueBounds.x, ribbonBlueBounds.y, ribbonBlueBounds.width, ribbonBlueBounds.height, null);

        g2.drawImage(mainMenuHover.get(TOGGLE_YELLOW) || ribbonSelected.get(TOGGLE_YELLOW)
                        ? avatarYellowPressed : avatarYellow,
                ribbonYellowBounds.x, ribbonYellowBounds.y, ribbonYellowBounds.width, ribbonYellowBounds.height, null);

        g2.drawImage(mainMenuHover.get(TOGGLE_RED) || ribbonSelected.get(TOGGLE_RED)
                        ? avatarRedPressed : avatarRed,
                ribbonRedBounds.x, ribbonRedBounds.y, ribbonRedBounds.width, ribbonRedBounds.height, null);

        g2.drawImage(mainMenuHover.get(TOGGLE_PURPLE) || ribbonSelected.get(TOGGLE_PURPLE)
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
        g2.fillRect(0, 0, screenConfig.SCREEN_WIDTH(), screenConfig.SCREEN_HEIGHT());

        PauseMenuLayout layout = getPauseMenuLayout();
        Rectangle ribbonBounds = layout.pauseRibbonBounds();
        Rectangle textBounds = layout.pauseTextBounds();
        Rectangle resumeBounds = layout.resumeBounds();
        Rectangle settingsBounds = layout.settingsBounds();
        Rectangle saveBounds = layout.saveBounds();

        // Pause text
        pauseBanner.draw(g2, ribbonBounds.x, ribbonBounds.y, ribbonBounds.width);
        String title = "PAUSE";
        g2.setColor(new Color(60, 40, 20, 200));
        g2.setFont(maruMonica.deriveFont(Font.BOLD, (float) UIConfig.PAUSE_TITLE_FONT_SIZE));
        Rectangle2D fontBounds = g2.getFontMetrics().getStringBounds(title, g2);
        int textX = textBounds.x + (int) Math.round((textBounds.width - fontBounds.getWidth()) / 2.0 - fontBounds.getX());
        int textY = textBounds.y + (int) Math.round((textBounds.height - fontBounds.getHeight()) / 2.0 - fontBounds.getY());
        g2.drawString(title, textX, textY);


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

        // -------------------------------------------------------
        // SFONDO PANNELLO SETTINGS (overlay semitrasparente)
        // -------------------------------------------------------
        Rectangle sb = layout.settingsBounds();
        g2.setColor(new Color(209, 205, 180));
        g2.fillRoundRect(sb.x, sb.y, sb.width, sb.height, 20, 20);
        g2.setColor(new Color(30, 30, 80));
        g2.setStroke(new BasicStroke(7));
        g2.drawRoundRect(sb.x, sb.y, sb.width, sb.height, 20, 20);
        g2.setStroke(new BasicStroke(1)); // reset stroke

        // Icona settings (gear)
        Rectangle settingsBounds = layout.settingsIconBounds();
        g2.drawImage(settingsHover.get(SETTINGS_ICON) || settingsSelected.get(SETTINGS_ICON)
                        ? settingsIconPressed : settingsIcon,
                settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);

        drawClouds(sb.x, sb.y, sb.width, sb.height, cloudPlacementsSettings);

        // -------------------------------------------------------
        // SEZIONE AUDIO — ribbon + icone
        // -------------------------------------------------------
        Rectangle audioRibbon = layout.audioRibbonBounds();
        redRibbon.draw(g2, audioRibbon.x, audioRibbon.y, audioRibbon.width, audioRibbon.height);

        Rectangle musicB = layout.musicBounds();
        g2.drawImage(settingsHover.get(MUSIC) || musicSelected.get(MUSIC)
                        ? buttonMusicSelected : buttonMusic,
                musicB.x, musicB.y, musicB.width, musicB.height, null);

        Rectangle soundB = layout.soundBounds();
        g2.drawImage(settingsHover.get(SOUND) || soundSelected.get(SOUND)
                        ? buttonSoundSelected : buttonSound,
                soundB.x, soundB.y, soundB.width, soundB.height, null);

        // -------------------------------------------------------
        // SEZIONE SCREEN — ribbon + bottoni FULL / MID / SMAL
        // -------------------------------------------------------
        Rectangle resRibbon = layout.resRibbonBounds();
        blueRibbon.draw(g2, resRibbon.x, resRibbon.y, resRibbon.width, resRibbon.height);

        drawButton(menuButton, menuButtonSelected,
                layout.resFullBounds().x, layout.resFullBounds().y,
                layout.resFullBounds().width, layout.resFullBounds().height,
                "FULL", settingsHover.get(RES_FULL) || screenSeclected.get(RES_FULL));

        drawButton(menuButton, menuButtonSelected,
                layout.resHalfBounds().x, layout.resHalfBounds().y,
                layout.resHalfBounds().width, layout.resHalfBounds().height,
                "MID", settingsHover.get(RES_MID) || screenSeclected.get(RES_MID));

        drawButton(menuButton, menuButtonSelected,
                layout.resMinBounds().x, layout.resMinBounds().y,
                layout.resMinBounds().width, layout.resMinBounds().height,
                "SMAL", settingsHover.get(RES_MIN) || screenSeclected.get(RES_MIN));

        // -------------------------------------------------------
        // SEZIONE FPS — ribbon + bottoni 60 / 120 / 240
        // -------------------------------------------------------
        Rectangle fpsRibbon = layout.fpsRibbonBounds();
        yellowRibbon.draw(g2, fpsRibbon.x, fpsRibbon.y, fpsRibbon.width, fpsRibbon.height);

        drawButton(menuButton, menuButtonSelected,
                layout.fpsBounds1().x, layout.fpsBounds1().y,
                layout.fpsBounds1().width, layout.fpsBounds1().height,
                "60", settingsHover.get(FPS_60) || fpsSeclected.get(FPS_60));

        drawButton(menuButton, menuButtonSelected,
                layout.fpsBounds2().x, layout.fpsBounds2().y,
                layout.fpsBounds2().width, layout.fpsBounds2().height,
                "120", settingsHover.get(FPS_120) || fpsSeclected.get(FPS_120));

        drawButton(menuButton, menuButtonSelected,
                layout.fpsBounds3().x, layout.fpsBounds3().y,
                layout.fpsBounds3().width, layout.fpsBounds3().height,
                "240", settingsHover.get(FPS_240) || fpsSeclected.get(FPS_240));

    }

    //-------------------------------------------------------------


    //-------------------------------------------------------------
    private void drawGameOverScreen() {
        drawPlayerLife();

        // Dark semi-transparent overlay
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g2.setColor(new Color(8, 8, 8));
        g2.fillRect(0, 0, screenConfig.SCREEN_WIDTH(), screenConfig.SCREEN_HEIGHT());
        g2.setComposite(oldComposite);

        // Title ribbon
        int titleRibbonWidth = 520;
        int titleRibbonHeight = 80;
        int titleRibbonX = (screenConfig.SCREEN_WIDTH() - titleRibbonWidth) / 2;
        int titleRibbonY = 72;
        ribbonBlueWide.draw(g2, titleRibbonX, titleRibbonY, titleRibbonWidth, titleRibbonHeight);

        // "GAME OVER" text centred inside the ribbon
        g2.setColor(Color.WHITE);
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 70F));
        String title = "GAME OVER";
        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(title, g2);
        int textX = getXforCenteredText(title);
        int textY = titleRibbonY + (int) Math.round((titleRibbonHeight - textBounds.getHeight()) / 2.0 - textBounds.getY());
        g2.drawString(title, textX, textY);

        // Restart button
        GameOverLayout layout = getGameOverLayout();
        Rectangle newGameBounds = layout.newGameBounds();
        drawButton(menuButton, menuButtonSelected, newGameBounds.x, newGameBounds.y, newGameBounds.width, newGameBounds.height,
                "New Game", gameOverHover.get(RESTART) || gameOverSelected.get(RESTART));
    }
    //-------------------------------------------------------------


    /**
     * Draws a menu button sprite with a horizontally and vertically centred label. */
    //-------------------------------------------------------------
    private void drawButton(SliceSprite menuButton, SliceSprite menuButtonSelected, int x, int y, int width, int height, String label, boolean selected) {
        (selected ? menuButtonSelected : menuButton).draw(g2, x, y, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 50F));

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
        int w = screenConfig.SCREEN_WIDTH();
        int h = screenConfig.SCREEN_HEIGHT();

        int alphaStep = Math.max(0, Math.min(DAMAGE_ALPHA_STEPS, Math.round(alpha * DAMAGE_ALPHA_STEPS)));
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
        if (gameModel.isDebugMode() && elapsedMs >= 4) {
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
        int centerX = screenConfig.SCREEN_WIDTH() / 2;
        int firstY = (screenConfig.SCREEN_HEIGHT() / 2) + screenConfig.TILE_SIZE();
        int gap = UIConfig.MENU_PADDING;

        Rectangle newGameBounds  = new Rectangle(centerX - buttonWidth / 2, firstY, buttonWidth, buttonHeight);
        Rectangle continueBounds = new Rectangle(centerX - buttonWidth / 2, firstY + buttonHeight + gap, buttonWidth, buttonHeight);

        int settingsSize = UIConfig.MENU_BUTTON_SETTINGS_SIZE;
        Rectangle settingsBounds = new Rectangle(screenConfig.SCREEN_WIDTH() - settingsSize - UIConfig.MENU_PADDING,
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
        int buttonWidth  = 320;
        int buttonHeight = 84;
        int centerX      = screenConfig.SCREEN_WIDTH() / 2;
        int buttonY      = screenConfig.SCREEN_HEIGHT() - buttonHeight - 56;
        return new GameOverLayout(new Rectangle(centerX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight));
    }
    //-------------------------------------------------------------
    public PauseMenuLayout getPauseMenuLayout() {

        int centerX = screenConfig.SCREEN_WIDTH() / 2;
        int centerY = screenConfig.SCREEN_HEIGHT() / 2;

        int bannerWidth  = UIConfig.BANNER_WIDTH;
        int bannerHeight = pauseBanner.getImageHeight();
        int bannerX = centerX - bannerWidth / 2;
        int bannerY = centerY - bannerHeight / 2 - UIConfig.PAUSE_RIBBON_OFFSET_Y;

        Rectangle pauseRibbonBounds = new Rectangle(bannerX, bannerY, bannerWidth, bannerHeight);
        Rectangle pauseTextBounds   = new Rectangle(bannerX, bannerY, bannerWidth, bannerHeight);

        int resumButtonWidth  = UIConfig.RESUME_BUTTON_WIDTH;
        int resumeButtonHeight = UIConfig.RESUME_BUTTON_HEIGHT;
        int saveButtonWidth  = UIConfig.SAVE_BUTTON_WIDTH;
        int saveButtonHeight = UIConfig.SAVE_BUTTON_HEIGHT;
        int gap = UIConfig.PAUSE_PADDING;
        int firstButtonY = bannerY + bannerHeight + gap;

        Rectangle resumeBounds = new Rectangle(centerX - resumButtonWidth / 2, firstButtonY, resumButtonWidth, resumeButtonHeight);
        Rectangle saveBounds   = new Rectangle(centerX - saveButtonWidth / 2, firstButtonY + saveButtonHeight + gap, saveButtonWidth, saveButtonHeight);

        int settingsSize = UIConfig.MENU_BUTTON_SETTINGS_SIZE;
        Rectangle settingsBounds = new Rectangle(screenConfig.SCREEN_WIDTH() - settingsSize - UIConfig.MENU_PADDING,
                UIConfig.MENU_PADDING,
                settingsSize,
                settingsSize);

        return new PauseMenuLayout(resumeBounds, settingsBounds, saveBounds, pauseTextBounds, pauseRibbonBounds);
    }
    //-------------------------------------------------------------
    public SettingsLayout getSettingsLayout() {

        int sw = screenConfig.SCREEN_WIDTH();
        int sh = screenConfig.SCREEN_HEIGHT();

        // --- SFONDO SETTINGS ---
        int settingsW = (int) (sw * 0.98f);
        int settingsH = (int) (sh * 0.98f);
        int settingsX = (sw - settingsW) / 2;
        int settingsY = (sh - settingsH) / 2;
        Rectangle settingsBounds = new Rectangle(settingsX, settingsY, settingsW, settingsH);

        // --- ICONA SETTINGS (gear) in alto a destra ---
        int settingsSize = UIConfig.MENU_BUTTON_SETTINGS_SIZE;
        Rectangle settingsIconBounds = new Rectangle(
                sw - settingsSize - UIConfig.MENU_PADDING,
                UIConfig.MENU_PADDING,
                settingsSize,
                settingsSize
        );

        // ---- RIBBON AUDIO (sezione in cima) ----
        int ribbonW = (int) (settingsW * 0.55f);
        int ribbonH = UIConfig.SETTINGS_RIBBON_HEIGHT; // es. 2 * TILE_SIZE
        int ribbonX = settingsX + (settingsW - ribbonW) / 2;

        int audioRibbonY = settingsY + UIConfig.MENU_PADDING;
        Rectangle audioRibbonBounds = new Rectangle(ribbonX, audioRibbonY, ribbonW, ribbonH);

        // Testo dentro il ribbon AUDIO
        Rectangle audioTextBounds = new Rectangle(ribbonX, audioRibbonY, ribbonW, ribbonH);

        // ---- ICONE AUDIO (music note + speaker) ----
        int iconSize = UIConfig.SETTINGS_ICON_SIZE; // es. 2 * TILE_SIZE
        int iconsY = audioRibbonY + ribbonH + UIConfig.MENU_PADDING;
        int totalIconsW = iconSize * 2 + UIConfig.MENU_PADDING;
        int iconsStartX = settingsX + (settingsW - totalIconsW) / 2;

        Rectangle musicBounds = new Rectangle(iconsStartX, iconsY, iconSize, iconSize);
        Rectangle soundBounds = new Rectangle(iconsStartX + iconSize + UIConfig.MENU_PADDING, iconsY, iconSize, iconSize);

        // ---- RIBBON SCREEN ----
        int screenRibbonY = iconsY + iconSize + UIConfig.MENU_PADDING * 2;
        Rectangle resRibbonBounds = new Rectangle(ribbonX, screenRibbonY, ribbonW, ribbonH);
        Rectangle resTextBounds = new Rectangle(ribbonX, screenRibbonY, ribbonW, ribbonH);

        // ---- BOTTONI SCREEN: FULL | MID | SMAL ----
        int btnW = UIConfig.SETTINGS_BUTTON_WIDTH;   // es. ~(settingsW / 4)
        int btnH = UIConfig.SETTINGS_BUTTON_HEIGHT;  // es. TILE_SIZE
        int btnY = screenRibbonY + ribbonH + UIConfig.MENU_PADDING;

        int totalBtnsW = btnW * 3 + UIConfig.MENU_PADDING * 2;
        int btnsStartX = settingsX + (settingsW - totalBtnsW) / 2;

        Rectangle resFullBounds = new Rectangle(btnsStartX, btnY, btnW, btnH);
        Rectangle resHalfBounds = new Rectangle(btnsStartX + btnW + UIConfig.MENU_PADDING, btnY, btnW, btnH);
        Rectangle resMinBounds = new Rectangle(btnsStartX + (btnW + UIConfig.MENU_PADDING) * 2, btnY, btnW, btnH);

        // ---- RIBBON FPS ----
        int fpsRibbonY = btnY + btnH + UIConfig.MENU_PADDING * 2;
        Rectangle fpsRibbonBounds = new Rectangle(ribbonX, fpsRibbonY, ribbonW, ribbonH);
        Rectangle fpsTextBounds = new Rectangle(ribbonX, fpsRibbonY, ribbonW, ribbonH);

        // ---- BOTTONI FPS: 60 | 120 | 240 ----
        int fpsBtnY = fpsRibbonY + ribbonH + UIConfig.MENU_PADDING;
        Rectangle fpsBounds1 = new Rectangle(btnsStartX, fpsBtnY, btnW, btnH);
        Rectangle fpsBounds2 = new Rectangle(btnsStartX + btnW + UIConfig.MENU_PADDING, fpsBtnY, btnW, btnH);
        Rectangle fpsBounds3 = new Rectangle(btnsStartX + (btnW + UIConfig.MENU_PADDING) * 2, fpsBtnY, btnW, btnH);

        return new SettingsLayout(
                settingsBounds,
                settingsIconBounds,
                audioTextBounds,
                audioRibbonBounds,
                musicBounds,
                soundBounds,
                fpsRibbonBounds,
                fpsTextBounds,
                fpsBounds1,
                fpsBounds2,
                fpsBounds3,
                resTextBounds,
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

        Player player = gameModel.getPlayer();
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
        return screenConfig.SCREEN_WIDTH() / 2 - length / 2;
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


    //BUTOTN STATE MANAGE
    //-------------------------------------------------------------
    private boolean isActive(Map<?, Boolean> map, Object key) {
        return (map.get(key));
    }
    //-------------------------------------------------------------




    // SETTER
    //-------------------------------------------------------------
    public void triggerDamageFlash() {
        // Starts a 0.5-second red damage flash overlay.
        damageFlashStartNano = System.nanoTime();
    }

    /*
    public void setMainMenuSelection(int mainMenuSelection) {
        this.mainMenuSelection = mainMenuSelection;
    }
    public void setHoveredRibbon(int hoveredRibbon) {
        this.hoveredRibbon = hoveredRibbon;
    }
    public void setActiveRibbon(int activeRibbon) {
        this.activeRibbon = activeRibbon;
    }
    public void setHoveredGameOverButton(boolean hoveredGameOverButton) {
        this.hoveredGameOverButton = hoveredGameOverButton;
    }
    public void setPauseMenuSelection(int pauseMenuSelection) {
        this.pauseMenuSelection = pauseMenuSelection;
    }
    public void setSettingsSelection(int settingsSelection) {}
*/

    // MainMenu hover: un solo pulsante attivo, null = nessuno
    public void setMainMenuHover(ButtonValue.MainMenu key) {
        mainMenuHover.replaceAll((k, v) -> key != null && k == key);
    }

    // MainMenu selected (navigazione tastiera) — solo bottoni normali, NON tocca i ribbon
    public void setMainMenuSelected(ButtonValue.MainMenu key) {
        mainMenuSelected.replaceAll((k, v) -> key != null && k == key);
    }

    // Ribbon selected — map separata, un click su ribbon non azzera i bottoni normali e viceversa
    public void setRibbonSelected(ButtonValue.MainMenu key) {
        ribbonSelected.replaceAll((k, v) -> key != null && k == key);
    }

    // Pause hover
    public void setPauseHover(ButtonValue.Pause key) {
        pauseHover.replaceAll((k, v) -> key != null && k == key);
    }

    // Pause selected
    public void setPauseSelected(ButtonValue.Pause key) {
        pauseSelected.replaceAll((k, v) -> key != null && k == key);
    }

    // Settings hover
    public void setSettingsHover(ButtonValue.Settings key) {
        settingsHover.replaceAll((k, v) -> key != null && k == key);
    }

    // Settings selected
    public void setSettingsSelected(ButtonValue.Settings key) {
        settingsSelected.replaceAll((k, v) -> key != null && k == key);
    }

    // GameOver hover
    public void setGameOverHover(ButtonValue.GameOver key) {
        gameOverHover.replaceAll((k, v) -> key != null && k == key);
    }

    // GameOver selected
    public void setGameOverSelected(ButtonValue.GameOver key) {
        gameOverSelected.replaceAll((k, v) -> key != null && k == key);
    }

    public void setSettingsMusicSelected(ButtonValue.Settings key) {
        musicSelected.replaceAll((k, v) -> key != null && k == key);
    }

    public void setSettingsSoundSelected(ButtonValue.Settings key) {
        soundSelected.replaceAll((k, v) -> key != null && k == key);
    }
    public void setSettingsFpsSelected(ButtonValue.Settings key) {
        fpsSeclected.replaceAll((k, v) -> key!= null && k == key);
    }
    public void setSettingsScreenResSelected(ButtonValue.Settings key) {
        settingsSelected.replaceAll((k, v) -> k != null && k == key);
    }


    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
// end class UI