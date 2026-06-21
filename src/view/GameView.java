package view;

import main.CONFIG.EntityConfig;
import main.CONFIG.GameConfig;
import main.CONFIG.MapConfig;
import main.CONFIG.ScreenConfig;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.ButtonValue;
import main.CONFIG.enu.GameState;
import model.IRenderable;
import model.event.AudioEventType;
import view.UI.*;
import view.audio.GameAudioManager;
import view.renderer.map.MapRender;
import view.renderer.map.TileSet;
import view.renderer.RenderDispatcher;

import javax.swing.*;

import controller.IController;

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

    private int gameWidth;
    private int gameHeight;

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

        gameHeight = screenHeight /2;
        gameWidth = screenWidth /2;
        setResolution();

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        applyCustomCursor(UIConfig.CURSOR_PATH);

        //  import the tileset Asset
        System.out.println("tileset path: " + GS.TILESET_IMG_PATH());
        this.tileSet = new TileSet(GS.TILESET_IMG_PATH(), GS.mapConfig().ORIGINAL_TILESIZE(), MapConfig.MAX_TILESET_ROW, MapConfig.MAX_TILESET_COL, GS.tilesetDoc());

        //import the map Render
        this.mapRender = new MapRender();

        // import the audio manager
        this.audioManager = new GameAudioManager();
        this.audioManager.syncBackgroundMusic(controller.getGameState());


        this.renderDispatcher = new RenderDispatcher(GS, controller.getPlayerColor());

        //initialize after 
        this.ui_render = new UI(controller, screenCfg, screenWidth, screenHeight);
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

        switch (controller.getGameState()) {

            case PLAYING, PAUSED, GAME_OVER, WIN -> {

                // DRAW THE WORLD MAP
                mapRender.DrawMap(screenCfg, controller.getWorldMap(), tileSet, controller.getPlayerWorldX(), controller.getPlayerWorldY(), g2, screenWidth, screenHeight);
                // Y-sorting logic: sort objects by their worldY coordinate
                drawEntities(g2);

                if (controller.isDebugMode()) {
                    drawWorldDebug(g2);
                }
            }
            case SETTINGS -> {
                if(controller.isSoundEnabled()){
                    audioManager.setSfxVolume(1);
                }else{
                    audioManager.setSfxVolume(0);
                }
                if (controller.isMusicEnabled()) {
                    audioManager.setMusicVolume(1);
                }else {
                    audioManager.setMusicVolume(0);
                }
            }
        }

        // draw the UI
        ui_render.draw(g2);
        g2.dispose();

    }

    //-------------------------------------------------------------

    private void drawWorldDebug(Graphics2D g2) {
        mapRender.drawAllGameLayers(controller.getWorldMap(), controller.getPlayerWorldX(), controller.getPlayerWorldY(), g2, screenWidth, screenHeight);
        //objectRenderer.drawDebugSolidAreas(g2, model.getObjects(), playerWorldX, playerWorldY, screenCfg, screenWidth, screenHeight);
    }

    //--------------------------------------------------------------
   
    private void drawEntities(Graphics2D g2){

        // Player's coordinates
        int playerWorldX = controller.getPlayerWorldX();
        int playerWorldY = controller.getPlayerWorldY();
        int pScreenX = screenWidth / 2 - (screenCfg.TILE_SIZE() / 2);
        int pScreenY = screenHeight / 2 - (screenCfg.TILE_SIZE() / 2);

        // Renderable entities and object
        List<IRenderable> renderList = new ArrayList<>(controller.getAllRenderables());

        // Universal y-sorting
        renderList.sort(Comparator.comparingInt(obj -> obj.getWorldY() + obj.getSolidArea().y + obj.getSolidArea().height));

        // Render
        for (IRenderable obj : renderList) {
        
            // Screen Coordinates
            int screenX = obj.getWorldX() - playerWorldX + pScreenX;
            int screenY = obj.getWorldY() - playerWorldY + pScreenY;

            // Universal culling: If the object is off-screen, we don't waste resources drawing it.
            if (screenX + obj.getWidth() < 0 || screenX > screenWidth ||
                screenY + obj.getHeight() < 0 || screenY > screenHeight) {
                continue;
            }

            renderDispatcher.draw(g2, obj, screenX, screenY, controller.isDebugMode(), playerWorldX, playerWorldY, controller.getPlayerCurrentLayer());

        }
    }

    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        renderDispatcher.update(controller, deltaMs);
    }
    //-------------------------------------------------------------

    private void applyResolutionValuesOnly() {
        switch (controller.getResolutionValue()) {
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

        switch (controller.getResolutionValue()) {
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

        System.out.println("screenWidth: " + screenWidth + " screenHeight: " + screenHeight);
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
        renderDispatcher.updatePlayerColor(controller.getPlayerColor());
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
        if (point == null) return null;
        switch (screen) {
            case MENU -> {
                MainMenuLayout l = ui_render.getMainMenuLayout();
                if (contains(l.newGameBounds(),     point)) return ButtonValue.MainMenu.NEW_GAME;
                if (contains(l.continueBounds(),    point)) return ButtonValue.MainMenu.LOAD_GAME;
                if (contains(l.settingsBounds(),    point)) return ButtonValue.MainMenu.SETTINGS;
                if (contains(l.toggleBlueBounds(),  point)) return ButtonValue.MainMenu.TOGGLE_BLUE;
                if (contains(l.toggleYellowBounds(),point)) return ButtonValue.MainMenu.TOGGLE_YELLOW;
                if (contains(l.toggleRedBounds(),   point)) return ButtonValue.MainMenu.TOGGLE_RED;
                if (contains(l.togglePurpleBounds(),point)) return ButtonValue.MainMenu.TOGGLE_PURPLE;
            }
            case PAUSED -> {
                PauseMenuLayout l = ui_render.getPauseMenuLayout();
                if (contains(l.resumeBounds(),   point)) return ButtonValue.PauseMenu.RESUME;
                if (contains(l.settingsBounds(), point)) return ButtonValue.PauseMenu.PAUSE_SETTINGS;
                if (contains(l.saveBounds(),     point)) return ButtonValue.PauseMenu.SAVE;
            }
            case SETTINGS -> {
                SettingsLayout l = ui_render.getSettingsLayout();
                if (contains(l.settingsIconBounds(), point)) return ButtonValue.SettingsMenu.SETTINGS_ICON;
                if (contains(l.musicBounds(),        point)) return ButtonValue.SettingsMenu.MUSIC;
                if (contains(l.soundBounds(),        point)) return ButtonValue.SettingsMenu.SOUND;
                if (contains(l.resFullBounds(),      point)) return ButtonValue.SettingsMenu.RES_FULL;
                if (contains(l.resHalfBounds(),      point)) return ButtonValue.SettingsMenu.RES_MID;
                if (contains(l.resMinBounds(),       point)) return ButtonValue.SettingsMenu.RES_MIN;
                if (contains(l.quitBounds(),         point)) return ButtonValue.SettingsMenu.QUIT;
            }
            case GAME_OVER -> {
                GameOverLayout l = ui_render.getGameOverLayout();
                if (contains(l.homeButtonBounds(), point)) return ButtonValue.GameOverMenu.HOME_OVER;
                if (contains(l.quitButtonBounds(), point)) return ButtonValue.GameOverMenu.QUIT_OVER;
            }
            case WIN -> {
                WinLayout l = ui_render.getWinLayout();
                if (contains(l.homeButtonBounds(), point)) return ButtonValue.WinMenu.HOME_WIN;
                if (contains(l.quitButtonBounds(), point)) return ButtonValue.WinMenu.QUIT_WIN;
            }
        }
        return null;
    }

    /**
     * Checks if a rectangle contains a point
     * (used to check mouse hovers and clicks)
     */
    private boolean contains(Rectangle bounds, Point p) {
        return bounds != null && p != null && bounds.contains(p);
    }

    @Override
    public void render(){
        this.repaint();
    }

    // Getter
    public Cursor getCustomGameCursor() {
        return customGameCursor;
    }

}
//-------------------------------------------------------------------------------------------------------------------
