package view.UI;

import model.GameModel;
import model.entity.DynamiteProjectile;
import model.entity.EnemyDynamite;
import model.entity.EnemyTNT;
import model.entity.EnemyTorch;
import model.entity.Player;
import model.object.GameObject;
import model.object.OBJ_Tree;
import view.renderer.entity.DynamiteRender;
import view.renderer.entity.MonkRenderer;
import view.renderer.entity.PlayerRender;
import view.renderer.entity.TNTRenderer;
import view.renderer.entity.TorchRenderer;
import view.renderer.map.MapRender;
import main.CONFIG.ScreenConfig;
import main.CONFIG.MapConfig;
import main.CONFIG.UIConfig;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

//TODO rivedere i vari metodi
/**
 * Draws all in-game UI elements: HUD, menus, dialogue windows, and debug overlays.
 */
//-------------------------------------------------------------------------------------------------------------------
public class UI {

    // =========================================================================
    // Constants
    // =========================================================================


    /** Duration of the damage flash overlay in nanoseconds (0.5 s). */
    private static final long DAMAGE_FLASH_DURATION_NS = 1500_000_000L;
    private static final int DAMAGE_ALPHA_STEPS = 24;
    private static final float[] DAMAGE_GRADIENT_DIST = { 0.0f, 0.60f, 0.82f, 1.0f };

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final GameModel      gameModel;
    private final ScreenConfig   screenConfig;
    private final MapConfig      mapConfig;
    private final PlayerRender   playerRenderer;
    private final TNTRenderer    tntRenderer;
    private final MonkRenderer   monkRenderer;
    private final MapRender      mapRender;
    private final DynamiteRender dynamiteRenderer;
    private final TorchRenderer  torchRenderer;

    // =========================================================================
    // Active rendering context
    // =========================================================================

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

    private final BufferedImage   menuLogo;
    private final BufferedImage   settingsIcon;
    private final BufferedImage   settingsIconPressed;
    private final BufferedImage   ribbonYellow;
    private final BufferedImage   ribbonRed;
    private final BufferedImage   ribbonBlue;
    private final BufferedImage   ribbonYellowPressed;
    private final BufferedImage   ribbonRedPressed;
    private final BufferedImage   ribbonBluePressed;
    private final BufferedImage[] menuClouds;

    // =========================================================================
    // FPS counter — updated once per second in debug mode
    // =========================================================================

    private long fpsTimer = System.nanoTime();
    private int  frames   = 0;
    private int  fps      = 0;

    // =========================================================================
    // Damage flash state
    // =========================================================================

    private long damageFlashStartNano = -1L;
    private int mainMenuSelection = UIConfig.MENU_DEFAULT_SELECTION;
    private int hoveredRibbon = UIConfig.MENU_NO_SELECTION;
    private int activeRibbon = UIConfig.MENU_NO_SELECTION;
    private boolean hoveredGameOverButton = false;
    private BufferedImage damageOverlayCache;
    private int damageOverlayCacheWidth = -1;
    private int damageOverlayCacheHeight = -1;
    private int damageOverlayAlphaStep = -1;

    // =========================================================================
    // Constructor
    // =========================================================================

    //-------------------------------------------------------------
    public UI(GameModel gameModel, PlayerRender playerRenderer, MapRender mapRender,
              ScreenConfig screenConfig, MapConfig mapConfig,
              TNTRenderer tntRenderer, MonkRenderer monkRenderer, DynamiteRender dynamiteRenderer, TorchRenderer torchRenderer) {

        this.gameModel      = gameModel;
        this.playerRenderer   = playerRenderer;
        this.mapRender      = mapRender;
        this.screenConfig   = screenConfig;
        this.mapConfig      = mapConfig;
        this.tntRenderer    = tntRenderer;
        this.monkRenderer   = monkRenderer;
        this.dynamiteRenderer = dynamiteRenderer;
        this.torchRenderer = torchRenderer;

        maruMonica = loadFont("/res/fonts/x12y16pxMaruMonica.ttf");

        int tileSize = screenConfig.TILE_SIZE();
        heartFull  = scaleImage(loadUiImage("src/res/UI/heart/heart_full.png"),  tileSize, tileSize);
        heartHalf  = scaleImage(loadUiImage("src/res/UI/heart/heart_half.png"),  tileSize, tileSize);
        heartBlank = scaleImage(loadUiImage("src/res/UI/heart/heart_blank.png"), tileSize, tileSize);

        menuButton         = new SliceSprite("src/res/UI/Buttons/Button_Blue_3Slides.png",  21, 21);
        menuButtonSelected = new SliceSprite("src/res/UI/Buttons/Button_Hover_3Slides.png", 21, 21);
        ribbonBlueWide     = new SliceSprite("src/res/UI/Ribbons/Ribbon_Blue_3Slides.png",  64, 64);
        pauseBanner        = new SliceSprite("src/res/UI/Banners/Banner_Horizontal.png",    192 / 3, 192 / 3);
        dialogueBanner     = new SliceSprite("src/res/UI/Banners/Banner_Horizontal.png",    64, 64);

        menuLogo            = loadUiImage("src/res/UI/Icons/logo_gioco.png");
        settingsIcon        = scaleImage(loadUiImage("src/res/UI/Icons/Regular_02.png"), 56, 56);
        settingsIconPressed = scaleImage(loadUiImage("src/res/UI/Icons/Pressed_02.png"), 56, 56);

        ribbonYellow        = loadUiImage("src/res/UI/Ribbons/Ribbon_Yellow_Connection_Right.png");
        ribbonRed           = loadUiImage("src/res/UI/Ribbons/Ribbon_Red_Connection_Right.png");
        ribbonBlue          = loadUiImage("src/res/UI/Ribbons/Ribbon_Blue_Connection_Right.png");
        ribbonYellowPressed = loadUiImage("src/res/UI/Ribbons/Ribbon_Yellow_Connection_Right_Pressed.png");
        ribbonRedPressed    = loadUiImage("src/res/UI/Ribbons/Ribbon_Red_Connection_Right_Pressed.png");
        ribbonBluePressed   = loadUiImage("src/res/UI/Ribbons/Ribbon_Blue_Connection_Right_Pressed.png");

        menuClouds = new BufferedImage[] {
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_01.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_02.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_03.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_04.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_05.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_06.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_07.png")),
                trimTransparentPadding(loadUiImage("src/res/UI/Clouds/Clouds_08.png"))
        };
    }
    //-------------------------------------------------------------

    // =========================================================================
    // Public API
    // =========================================================================

    /** Main draw entry-point — called once per frame from the game loop. */
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
        }

        if (gameModel.isDebugMode()) drawDebugOverlay();
    }
    //-------------------------------------------------------------

    /**
     * Starts a 0.5-second red damage flash overlay.
     * Call this from the model or player whenever the player receives damage.
     * The overlay fades automatically; no reset is needed.
     */
    //-------------------------------------------------------------
    public void triggerDamageFlash() {
        damageFlashStartNano = System.nanoTime();
    }
    //-------------------------------------------------------------

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

    // =========================================================================
    // Screen draw methods
    // =========================================================================

    //-------------------------------------------------------------
    private void drawMainMenu() {
        int w = screenConfig.SCREEN_WIDTH();
        int h = screenConfig.SCREEN_HEIGHT();

        g2.setColor(new Color(83, 189, 191));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(140, 224, 228));
        g2.drawRect(0, 0, w - 1, h - 1);
        drawMenuClouds(0, 0, w, h);

        int logoWidth  = 500;
        int logoHeight = (int) (((double) menuLogo.getHeight() / menuLogo.getWidth()) * logoWidth);
        g2.drawImage(menuLogo, (w - logoWidth) / 2, 40, logoWidth, logoHeight, null);

        MainMenuLayout layout       = getMainMenuLayout();
        int selectedItem            = mainMenuSelection;
        int hoveredRibbon           = this.hoveredRibbon;
        int activeRibbon            = this.activeRibbon;

        Rectangle newGameBounds      = layout.newGameBounds();
        Rectangle continueBounds     = layout.continueBounds();
        Rectangle settingsBounds     = layout.settingsBounds();
        Rectangle ribbonYellowBounds = layout.ribbonYellowBounds();
        Rectangle ribbonRedBounds    = layout.ribbonRedBounds();
        Rectangle ribbonBlueBounds   = layout.ribbonBlueBounds();

        g2.drawImage((hoveredRibbon == 0 || activeRibbon == 0) ? ribbonYellowPressed : ribbonYellow,
                ribbonYellowBounds.x, ribbonYellowBounds.y, ribbonYellowBounds.width, ribbonYellowBounds.height, null);
        g2.drawImage((hoveredRibbon == 1 || activeRibbon == 1) ? ribbonRedPressed : ribbonRed,
                ribbonRedBounds.x, ribbonRedBounds.y, ribbonRedBounds.width, ribbonRedBounds.height, null);
        g2.drawImage((hoveredRibbon == 2 || activeRibbon == 2) ? ribbonBluePressed : ribbonBlue,
                ribbonBlueBounds.x, ribbonBlueBounds.y, ribbonBlueBounds.width, ribbonBlueBounds.height, null);

        drawMenuButton(newGameBounds.x,  newGameBounds.y,  newGameBounds.width,  newGameBounds.height,  "New Game", selectedItem == 0);
        drawMenuButton(continueBounds.x, continueBounds.y, continueBounds.width, continueBounds.height, "Resume",   selectedItem == 1);

        g2.drawImage((selectedItem == 2) ? settingsIconPressed : settingsIcon,
                settingsBounds.x, settingsBounds.y, settingsBounds.width, settingsBounds.height, null);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    private void drawPauseScreen() {
        g2.setColor(screenConfig.GAME_BG_COLOR());
        g2.fillRect(0, 0, screenConfig.SCREEN_WIDTH(), screenConfig.SCREEN_HEIGHT());

        int bannerWidth  = 192 * 3;
        int bannerHeight = pauseBanner.getImageHeight();
        int bannerX      = (screenConfig.SCREEN_WIDTH()  - bannerWidth)  / 2;
        int bannerY      = (screenConfig.SCREEN_HEIGHT() - bannerHeight) / 2;
        pauseBanner.draw(g2, bannerX, bannerY, bannerWidth);

        g2.setColor(Color.WHITE);
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 80));
        g2.drawString("PAUSED", getXforCenteredText("PAUSED"), screenConfig.SCREEN_HEIGHT() / 2);
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
        int titleRibbonWidth  = 520;
        int titleRibbonHeight = 80;
        int titleRibbonX      = (screenConfig.SCREEN_WIDTH() - titleRibbonWidth) / 2;
        int titleRibbonY      = 72;
        ribbonBlueWide.draw(g2, titleRibbonX, titleRibbonY, titleRibbonWidth, titleRibbonHeight);

        // "GAME OVER" text centred inside the ribbon
        g2.setColor(Color.WHITE);
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 70F));
        String      title      = "GAME OVER";
        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(title, g2);
        int textX = getXforCenteredText(title);
        int textY = titleRibbonY + (int) Math.round((titleRibbonHeight - textBounds.getHeight()) / 2.0 - textBounds.getY());
        g2.drawString(title, textX, textY);

        // Restart button
        GameOverLayout layout        = getGameOverLayout();
        Rectangle      newGameBounds = layout.newGameBounds();
        drawMenuButton(newGameBounds.x, newGameBounds.y, newGameBounds.width, newGameBounds.height,
                "New Game", hoveredGameOverButton);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void drawDialogueWindow() {
        int width  = screenConfig.SCREEN_WIDTH() - (screenConfig.TILE_SIZE() * 2);
        int height = screenConfig.TILE_SIZE() * 4;
        int x      = (screenConfig.SCREEN_WIDTH() - width) / 2;
        int y      = screenConfig.SCREEN_HEIGHT() - height - screenConfig.TILE_SIZE();

        dialogueBanner.draw(g2, x, y, width);

        String dialogue = gameModel.getCurrentDialogue();
        if (dialogue == null || dialogue.isEmpty()) return;

        // Dialogue text
        g2.setColor(new Color(60, 40, 20));
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 28F));
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
    private void drawPlayerLife() {
        int playerLife  = gameModel.getPlayer().getLife();
        int totalHearts = (gameModel.getPlayer().getMaxLife() + 1) / 2;

        int heartWidth = heartFull.getWidth();
        int spacing    = Math.max(4, heartWidth / 6);

        for (int i = 0; i < totalHearts; i++) {
            int           lifeForHeart = playerLife - (i * 2);
            BufferedImage img          = (lifeForHeart >= 2) ? heartFull
                    : (lifeForHeart == 1) ? heartHalf
                      : heartBlank;
            g2.drawImage(img, 20 + i * (heartWidth + spacing), 20, null);
        }

        // Draw the damage flash only if the 0.5 s window is still active.
        if (isDamageFlashActive()) {
            float elapsed = (System.nanoTime() - damageFlashStartNano) / (float) DAMAGE_FLASH_DURATION_NS;
            float alpha   = 1.0f - Math.min(1.0f, elapsed); // linear fade-out
            drawDamageOverlay(alpha);
        }
    }
    //-------------------------------------------------------------

    // =========================================================================
    // Damage overlay
    // =========================================================================

    /**
     * Returns {@code true} while the damage flash window is open.
     * The window expires {@value #DAMAGE_FLASH_DURATION_NS} ns after the last
     * call to {@link #triggerDamageFlash()}.
     */
    //-------------------------------------------------------------
    private boolean isDamageFlashActive() { //TODO rivedere la catena di attivazione del danno
        return damageFlashStartNano >= 0
                && (System.nanoTime() - damageFlashStartNano) < DAMAGE_FLASH_DURATION_NS;
    }
    //-------------------------------------------------------------

    /**
     * Draws a red vignette + soft screen flash that fades out according to {@code alpha}.
     *
     * @param alpha master opacity in [0, 1]: 1 = fully visible (just hit), 0 = invisible (expired).
     */
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

    // =========================================================================
    // Layout computation
    // =========================================================================

    //-------------------------------------------------------------
    public MainMenuLayout getMainMenuLayout() {
        int buttonWidth  = 420;
        int buttonHeight = 96;
        int centerX      = screenConfig.SCREEN_WIDTH() / 2;
        int firstY       = (screenConfig.SCREEN_HEIGHT() / 2) + 58;
        int gap          = 24;

        Rectangle newGameBounds  = new Rectangle(centerX - buttonWidth / 2, firstY,                      buttonWidth, buttonHeight);
        Rectangle continueBounds = new Rectangle(centerX - buttonWidth / 2, firstY + buttonHeight + gap, buttonWidth, buttonHeight);

        int settingsSize = 56;
        Rectangle settingsBounds = new Rectangle(screenConfig.SCREEN_WIDTH() - settingsSize - 24, 24, settingsSize, settingsSize);

        int ribbonX = 20;
        int ribbonY = 16;
        int ribbonW = 64;
        int ribbonH = 64;
        Rectangle ribbonYellowBounds = new Rectangle(ribbonX, ribbonY,        ribbonW, ribbonH);
        Rectangle ribbonRedBounds    = new Rectangle(ribbonX, ribbonY + 52,   ribbonW, ribbonH);
        Rectangle ribbonBlueBounds   = new Rectangle(ribbonX, ribbonY + 104,  ribbonW, ribbonH);

        return new MainMenuLayout(newGameBounds, continueBounds, settingsBounds,
                ribbonYellowBounds, ribbonRedBounds, ribbonBlueBounds);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public GameOverLayout getGameOverLayout() {
        int buttonWidth  = 320;
        int buttonHeight = 84;
        int centerX      = screenConfig.SCREEN_WIDTH() / 2;
        int buttonY      = screenConfig.SCREEN_HEIGHT() - buttonHeight - 56;
        return new GameOverLayout(new Rectangle(centerX - buttonWidth / 2, buttonY, buttonWidth, buttonHeight));
    }
    //-------------------------------------------------------------

    // =========================================================================
    // Debug overlay
    // =========================================================================

    //-------------------------------------------------------------
    private void drawDebugOverlay() {
        int pScreenX = screenConfig.SCREEN_WIDTH() / 2 - (screenConfig.TILE_SIZE() / 2);
        int pScreenY = screenConfig.SCREEN_HEIGHT() / 2 - (screenConfig.TILE_SIZE() / 2);

        mapRender.drawAllGameLayers(gameModel.getWorldMap(), gameModel.getPlayer(), g2);

        playerRenderer.drawSolidArea(g2, gameModel.getPlayer(), pScreenX, pScreenY);

        for (EnemyDynamite ed : gameModel.getDynamiteEnemies()) {
            dynamiteRenderer.drawSolidArea(g2, ed, screenX(ed.getWorldX()), screenY(ed.getWorldY()));
        }
        for (Object proj : gameModel.getProjectiles()) {
            if (proj instanceof DynamiteProjectile dp) {
                dynamiteRenderer.drawProjectileSolidArea(g2, dp, screenX(dp.getWorldX()), screenY(dp.getWorldY()));
            }
        }
        for (EnemyTNT tnt : gameModel.getTntEnemies()) {
            tntRenderer.drawSolidArea(g2, tnt, screenX(tnt.getWorldX()), screenY(tnt.getWorldY()));
        }
        for (EnemyTorch torch : gameModel.getTorchEnemies()) {
            torchRenderer.drawSolidArea(g2, torch, screenX(torch.getWorldX()), screenY(torch.getWorldY()));
        }

        drawTreeSolidAreas();
        monkRenderer.drawSolidArea(g2, gameModel.getMonk(), screenX(gameModel.getMonk().getWorldX()), screenY(gameModel.getMonk().getWorldY()));
        drawFpsOverlay();
    }
    //-------------------------------------------------------------

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

    //-------------------------------------------------------------
    private void drawTreeSolidAreas() {
        for (GameObject obj : gameModel.getObjects()) {
            if (!(obj instanceof OBJ_Tree tree)) continue;
            if (!tree.isSolid() || tree.getSolidArea() == null) continue;

            Rectangle solid = tree.getSolidArea();
            int drawX = screenX(tree.getWorldX()) + solid.x;
            int drawY = screenY(tree.getWorldY()) + solid.y;

            g2.setColor(new Color(0, 255, 255, 80));
            g2.fillRect(drawX, drawY, solid.width, solid.height);
            g2.setColor(Color.CYAN);
            g2.drawRect(drawX, drawY, solid.width, solid.height);
        }
    }
    //-------------------------------------------------------------

    // =========================================================================
    // Private utility methods
    // =========================================================================

    //-------------------------------------------------------------
    private void drawMenuClouds(int panelX, int panelY, int panelW, int panelH) {
        if (menuClouds == null || menuClouds.length == 0) return;

        // Each entry: { relativeX, relativeY, drawWidth }
        float[][] placements = {
                { 0.12f, 0.16f, 180f },
                { 0.29f, 0.24f, 140f },
                { 0.63f, 0.16f, 185f },
                { 0.82f, 0.26f, 135f },
                { 0.11f, 0.49f, 175f },
                { 0.89f, 0.88f, 130f },
                { 0.78f, 0.53f, 165f },
                { 0.28f, 0.72f, 205f }
        };

        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));

        for (int i = 0; i < placements.length; i++) {
            BufferedImage cloud      = menuClouds[i % menuClouds.length];
            int           drawWidth  = Math.round(placements[i][2]);
            int           drawHeight = Math.max(1, Math.round(drawWidth * ((float) cloud.getHeight() / cloud.getWidth())));
            int           drawX      = panelX + Math.round(placements[i][0] * panelW) - drawWidth  / 2;
            int           drawY      = panelY + Math.round(placements[i][1] * panelH) - drawHeight / 2;
            g2.drawImage(cloud, drawX, drawY, drawWidth, drawHeight, null);
        }

        g2.setComposite(oldComposite);
    }
    //-------------------------------------------------------------

    /** Draws a menu button sprite with a horizontally and vertically centred label. */
    //-------------------------------------------------------------
    private void drawMenuButton(int x, int y, int width, int height, String label, boolean selected) {
        (selected ? menuButtonSelected : menuButton).draw(g2, x, y, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(maruMonica.deriveFont(Font.BOLD, 50F));
        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(label, g2);
        int textX = x + (int) Math.round((width - textBounds.getWidth()) / 2.0 - textBounds.getX());

        // The sprite has a heavier bottom shadow, so centre text on the visual body area.
        int contentY      = y + 4;
        int contentHeight = height - 35;
        int textY = contentY + (int) Math.round((contentHeight - textBounds.getHeight()) / 2.0 - textBounds.getY());
        g2.drawString(label, textX, textY);
    }
    //-------------------------------------------------------------

    /** Returns the X coordinate at which {@code text} will be horizontally centred on screen. */
    //-------------------------------------------------------------
    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return screenConfig.SCREEN_WIDTH() / 2 - length / 2;
    }
    //-------------------------------------------------------------

    private int screenX(int worldX) {
        Player p = gameModel.getPlayer();
        int pScreenX = screenConfig.SCREEN_WIDTH() / 2 - (screenConfig.TILE_SIZE() / 2);
        return worldX - p.getWorldX() + pScreenX;
    }

    private int screenY(int worldY) {
        Player p = gameModel.getPlayer();
        int pScreenY = screenConfig.SCREEN_HEIGHT() / 2 - (screenConfig.TILE_SIZE() / 2);
        return worldY - p.getWorldY() + pScreenY;
    }
    /** Returns a new {@link BufferedImage} that is a scaled copy of {@code original}. */
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
     * Returns the tightest sub-image of {@code source} that contains all
     * non-transparent pixels, stripping any empty transparent padding.
     */
    //-------------------------------------------------------------
    private static BufferedImage trimTransparentPadding(BufferedImage source) {
        int minX = source.getWidth(), minY = source.getHeight();
        int maxX = -1,               maxY = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                if (((source.getRGB(x, y) >>> 24) & 0xFF) > 0) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (maxX < minX || maxY < minY) return source;
        return source.getSubimage(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }
    //-------------------------------------------------------------

    /** Loads an image from the given file-system path, throwing on failure. */
    //-------------------------------------------------------------
    private BufferedImage loadUiImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load UI image: " + path, e);
        }
    }
    //-------------------------------------------------------------

    /** Loads a TrueType font from the given classpath resource, throwing on failure. */
    //-------------------------------------------------------------
    private Font loadFont(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException("Unable to load font: " + resourcePath, e);
        }
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
// end class UI
