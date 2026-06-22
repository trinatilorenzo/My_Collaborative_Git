package tinyswordsisland.view;

import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.MapConfig;
import tinyswordsisland.config.ScreenConfig;
import tinyswordsisland.config.UIConfig;
import tinyswordsisland.config.enu.ButtonValue;
import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.view.ui.*;
import tinyswordsisland.view.audio.GameAudioManager;
import tinyswordsisland.view.renderer.map.MapRender;
import tinyswordsisland.view.renderer.map.TileSet;
import tinyswordsisland.view.renderer.RenderDispatcher;

import javax.swing.*;

import tinyswordsisland.controller.IController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ALL THE RENDERING STAFF HERE
 * render, camera, asset( img animation, texture), sfx ..
 * GameView is responsible for rendering the game to the screen. This class extends JPanel
 * and contains methods and assets required for drawing the game world, player, and other
 * visual elements.
 */
//-------------------------------------------------------------------------------------------------------------------
public class GameView extends JPanel implements IGameView {

    private final ScreenConfig screenCfg;

    private int screenWidth;
    private int screenHeight;

    private final IController controller;

    // renderers
    private MapRender mapRender;
    private TileSet tileSet;
    private UI ui_render;
    private Cursor customGameCursor;


    private RenderDispatcher renderDispatcher;
    //audio
    private GameAudioManager audioManager;


    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameView(GameConfig GS, IController controller) {
        this.screenCfg = GS.screenConfig();
        this.controller = controller;

        setResolution();

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        applyCustomCursor(UIConfig.CURSOR_PATH);

        this.tileSet = new TileSet(GS.TILESET_IMG_PATH(), GS.mapConfig().ORIGINAL_TILESIZE(), MapConfig.MAX_TILESET_ROW, MapConfig.MAX_TILESET_COL, GS.tilesetDoc());
        this.mapRender = new MapRender();
        this.audioManager = new GameAudioManager();
        this.audioManager.syncBackgroundMusic(controller.snapshot().gameState());

        this.renderDispatcher = new RenderDispatcher(GS, controller.snapshot().playerColor());

        this.ui_render = new UI(screenCfg, screenWidth, screenHeight);
        setResolution();

    }
    //-------------------------------------------------------------

    private void applyCustomCursor(String cursorPath) {
        try {
            BufferedImage cursorImage = null;
            try (InputStream is = getClass().getResourceAsStream(cursorPath)) {
                if (is != null) {
                    cursorImage = ImageIO.read(is);
                }
            }
            if (cursorImage == null) {
                cursorImage = ImageIO.read(new File(cursorPath));
            }
            customGameCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "game_cursor");
            setCursor(customGameCursor);
        } catch (IOException e) {
            // If cursor asset cannot be loaded, keep default cursor.
        }
    }
    //-------------------------------------------------------------


    /**
     * where everything will be drawn
     */
    //-------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(screenCfg.GAME_BG_COLOR());
        g2.fillRect(0,0, screenWidth, screenHeight);

        GameViewState state = controller.snapshot();

        switch (state.gameState()) {

            case PLAYING, PAUSED, GAME_OVER, WIN -> {
                mapRender.DrawMap(screenCfg, state.worldMap(), tileSet, state.playerWorldX(), state.playerWorldY(), g2, screenWidth, screenHeight);
                drawEntities(g2, state);

                if (state.debugMode()) {
                    drawWorldDebug(g2, state);
                }
            }
            case SETTINGS -> {
                if (state.soundEnabled()) {
                    audioManager.setSfxVolume(1);
                } else {
                    audioManager.setSfxVolume(0);
                }
                if (state.musicEnabled()) {
                    audioManager.setMusicVolume(1);
                } else {
                    audioManager.setMusicVolume(0);
                }
            }
        }

        ui_render.draw(g2, state);
        g2.dispose();

    }

    //-------------------------------------------------------------

    private void drawWorldDebug(Graphics2D g2, GameViewState state) {
        mapRender.drawAllGameLayers(state.worldMap(), state.playerWorldX(), state.playerWorldY(), g2, screenWidth, screenHeight);
    }

    private void drawEntities(Graphics2D g2, GameViewState state) {
        int playerWorldX = state.playerWorldX();
        int playerWorldY = state.playerWorldY();
        int pScreenX = screenWidth / 2;
        int pScreenY = screenHeight / 2;

        List<IRenderable> renderList = new ArrayList<>(state.renderables());
        renderList.sort(Comparator.comparingInt(obj -> obj.getWorldY() + obj.getSolidArea().y + obj.getSolidArea().height));

        for (IRenderable obj : renderList) {
            int screenX = obj.getWorldX() - playerWorldX + pScreenX;
            int screenY = obj.getWorldY() - playerWorldY + pScreenY;

            if (screenX + obj.getWidth() < 0 || screenX > screenWidth
                    || screenY + obj.getHeight() < 0 || screenY > screenHeight) {
                continue;
            }

            renderDispatcher.draw(g2, obj, screenX, screenY, state.debugMode(), state.playerCurrentLayer());
        }
    }

    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        renderDispatcher.update(controller.snapshot().renderables(), deltaMs);
    }
    //-------------------------------------------------------------

    private void applyResolutionValuesOnly() {
        switch (controller.snapshot().resolutionValue()) {
            case 0 -> {
                screenWidth = screenCfg.MIN_SCREEN_WIDTH();
                screenHeight = screenCfg.MIN_SCREEN_HEIGHT();
            }
            case 1 -> {
                Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                screenWidth = r.width;
                screenHeight = r.height;
            }
            case 2 -> {
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                screenWidth = gd.getDisplayMode().getWidth();
                screenHeight = gd.getDisplayMode().getHeight();
            }
        }
    }

    public void setResolution() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);


        if (frame == null) {
            applyResolutionValuesOnly();
            setPreferredSize(new Dimension(screenWidth, screenHeight));

            if (ui_render != null) {
                ui_render.setScreenSize(screenWidth, screenHeight);
            }

            revalidate();
            repaint();
            return;
        }

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        if (gd.getFullScreenWindow() == frame) {
            gd.setFullScreenWindow(null);
        }

        //reset state
        frame.dispose();
        frame.setExtendedState(JFrame.NORMAL);

        switch (controller.snapshot().resolutionValue()) {
            case 0 -> {
                screenWidth = screenCfg.MIN_SCREEN_WIDTH();
                screenHeight = screenCfg.MIN_SCREEN_HEIGHT();

                frame.setUndecorated(false);
                frame.setResizable(false);

                setPreferredSize(new Dimension(screenWidth, screenHeight));
                setMinimumSize(new Dimension(screenWidth, screenHeight));
                setMaximumSize(new Dimension(screenWidth, screenHeight));

                frame.setContentPane(this);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                Dimension size = getSize();
                screenWidth = size.width;
                screenHeight = size.height;
            }

            case 1 -> {

                Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                frame.setBounds(r);

                frame.setUndecorated(false);
                frame.setResizable(false);
                frame.setVisible(true);

                Dimension d = frame.getContentPane().getSize();
                screenWidth = d.width;
                screenHeight = d.height;

            }

            case 2 -> {

                screenWidth = gd.getDisplayMode().getWidth();
                screenHeight = gd.getDisplayMode().getHeight();

                frame.setUndecorated(true);
                frame.setResizable(false);

                setPreferredSize(new Dimension(screenWidth, screenHeight));

                frame.setContentPane(this);
                frame.pack();
                frame.setVisible(true);

                if (gd.isFullScreenSupported()) {
                    gd.setFullScreenWindow(frame);
                } else {
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            }
        }

        if (ui_render != null) {
            ui_render.setScreenSize(screenWidth, screenHeight);
        }

        revalidate();
        repaint();
        frame.revalidate();
        frame.repaint();
    }
    //-------------------------------------------------------------

    public void onGameStateChanged(GameState gameState) {
        audioManager.syncBackgroundMusic(gameState);
    }

    public void processGameEvents(List<AudioEventType> events) {
        if (events.isEmpty()) {
            return;
        }
        for (AudioEventType event : events) {
            if (event == AudioEventType.PLAYER_DAMAGED) {
                ui_render.triggerDamageFlash();
                break;
            }
        }
        audioManager.playEvents(events);
    }

    public void shutdownAudio() {
        audioManager.stopAll();
    }

    public void updatePlayerColor() {
        renderDispatcher.updatePlayerColor(controller.snapshot().playerColor());
    }

    @Override
    public void applyMenuState(GameState screen, Enum<?> hovered, Enum<?> selected) {
        if (screen == null) return;
        if (hovered != null) {
            ui_render.setHover(hovered);
        } else {
            // reset hover per la categoria corrispondente allo screen
            Class<? extends Enum<?>> menuClass = menuClassForScreen(screen);
            if (menuClass != null) ui_render.resetHover(menuClass);
        }
        if (selected != null) {
            ui_render.setSelected(selected);
        }
    }

    private Class<? extends Enum<?>> menuClassForScreen(GameState screen) {
        return switch (screen) {
            case MENU      -> ButtonValue.MainMenu.class;
            case PAUSED    -> ButtonValue.PauseMenu.class;
            case SETTINGS  -> ButtonValue.SettingsMenu.class;
            case GAME_OVER -> ButtonValue.GameOverMenu.class;
            case WIN       -> ButtonValue.WinMenu.class;
            default        -> null;
        };
    }

    @Override
    public Enum<?> getButtonAtPoint(GameState screen, Point point) {
        return MenuHitTester.hitTest(screen, point, new MenuHitTester.MenuLayouts(
                ui_render::getMainMenuLayout,
                ui_render::getPauseMenuLayout,
                ui_render::getSettingsLayout,
                ui_render::getGameOverLayout,
                ui_render::getWinLayout
        ));
    }

    @Override
    public void render() {
        this.repaint();
    }

    // Getter
    public Cursor getCustomGameCursor() {
        return customGameCursor;
    }

}
//-------------------------------------------------------------------------------------------------------------------
