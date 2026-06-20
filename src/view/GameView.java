package view;

import main.CONFIG.EntityConfig;
import main.CONFIG.GameConfig;
import main.CONFIG.MapConfig;
import main.CONFIG.ScreenConfig;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.ButtonValue;
import main.CONFIG.enu.GameState;
import model.GameModel;
import model.IRenderable;
import model.entity.Player;
import model.event.AudioEventType;
import view.UI.*;
import view.audio.GameAudioManager;
import view.renderer.map.MapRender;
import view.renderer.map.TileSet;
import view.renderer.RenderDispatcher;

import javax.swing.*;
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
public class GameView extends JPanel {

    private final ScreenConfig screenCfg;

    private int screenWidth;
    private int screenHeight;

    private int gameWidth;
    private int gameHeight;

    //model to render
    private final GameModel model;

    // renderers
    private final MapRender mapRender;
    private final TileSet tileSet;
    private final UI ui_render;
    private Cursor customGameCursor;


    private final RenderDispatcher renderDispatcher;
    //audio
    private final GameAudioManager audioManager;


    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public GameView(GameConfig GS, GameModel model) {
        this.screenCfg = GS.screenConfig();
        this.model = model;

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
        this.audioManager.syncBackgroundMusic(model.getGameState());


        this.renderDispatcher = new RenderDispatcher(GS, model.getPlayerColor());
        //import the UI
        this.ui_render = new UI(model, screenCfg, screenWidth, screenHeight);
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

        switch (model.getGameState()) {

            case PLAYING, PAUSED, GAME_OVER, WIN -> {

                // DRAW THE WORLD MAP
                mapRender.DrawMap(screenCfg, model.getWorldMap(), tileSet, model.getPlayer(), g2, screenWidth, screenHeight);
                // Y-sorting logic: sort objects by their worldY coordinate
                drawEntities(g2);

                if (model.isDebugMode()) {
                    drawWorldDebug(g2);
                }
            }
            case SETTINGS -> {
                if(model.isSoundEnabled()){
                    audioManager.setSfxVolume(1);
                }else{
                    audioManager.setSfxVolume(0);
                }
                if (model.isMusicEnabled()) {
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
        Player player = model.getPlayer();
        mapRender.drawAllGameLayers(model.getWorldMap(), player, g2, screenWidth, screenHeight);
        //objectRenderer.drawDebugSolidAreas(g2, model.getObjects(), player, screenCfg, screenWidth, screenHeight);
    }

    //--------------------------------------------------------------
   
    private void drawEntities(Graphics2D g2){

        // Player's coordinates
        Player player = model.getPlayer();
        int pScreenX = screenWidth / 2 - (screenCfg.TILE_SIZE() / 2);
        int pScreenY = screenHeight / 2 - (screenCfg.TILE_SIZE() / 2);

        // Renderable entities and object
        List<IRenderable> renderList = new ArrayList<>(model.getAllRenderables());

        // Universal y-sorting
        renderList.sort(Comparator.comparingInt(obj -> obj.getWorldY() + obj.getSolidArea().y + obj.getSolidArea().height));

        // Render
        for (IRenderable obj : renderList) {
        
            // Screen Coordinates
            int screenX = obj.getWorldX() - player.getWorldX() + pScreenX;
            int screenY = obj.getWorldY() - player.getWorldY() + pScreenY;

            // Universal culling: If the object is off-screen, we don't waste resources drawing it.
            if (screenX + obj.getWidth() < 0 || screenX > screenWidth ||
                screenY + obj.getHeight() < 0 || screenY > screenHeight) {
                continue;
            }

            renderDispatcher.draw(g2, obj, screenX, screenY, model.isDebugMode());

        }
    }

    public void updateAnimations(double deltaMs) {
        tileSet.updateAnimTile(deltaMs);
        renderDispatcher.update(model, deltaMs);
    }
    //-------------------------------------------------------------

    private void applyResolutionValuesOnly() {
        switch (model.getResolutionValue()) {
            case 0 -> {
                screenWidth = screenCfg.MIN_SCREEN_WIDTH();
                screenHeight = screenCfg.MIN_SCREEN_HEIGHT();
            }
            case 1 -> {
                Rectangle r = GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();

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

        switch (model.getResolutionValue()) {
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

    public void processGameEvents() {
        List<AudioEventType> events = model.consumeAudioEvents();
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
        renderDispatcher.updatePlayerColor(model.getPlayerColor());
    }
   
    // GETTER ----------------------
    public MainMenuLayout getMainMenuLayout() {
        return ui_render.getMainMenuLayout();
    }
    public GameOverLayout getGameOverLayout() {
        return ui_render.getGameOverLayout();
    }
    public PauseMenuLayout getPauseMenuLayout() {return ui_render.getPauseMenuLayout();}
    public SettingsLayout getSettingsLayout() {return ui_render.getSettingsLayout();}
    public WinLayout getWinLayout() {return ui_render.getWinLayout();};
    public Cursor getCustomGameCursor() {
        return customGameCursor;
    }
    //---------------------------------

    //SETTER ----------------------
    public void setMainMenuHover(ButtonValue.MainMenu key) {
        ui_render.setMainMenuHover(key);
    }
    public void setMainMenuSelected(ButtonValue.MainMenu key) {
        ui_render.setMainMenuSelected(key);
    }
    public void resetMainMenuHover() {
        ui_render.resetMainMenuHover();
    }
    public void setPauseHover(ButtonValue.PauseMenu key) {
        ui_render.setPauseHover(key);
    }
    public void setPauseSelected(ButtonValue.PauseMenu key) {
        ui_render.setPauseSelected(key);
    }
    public void resetPauseHover(){
        ui_render.resetPauseHover();
    }
    public void setSettingsHover(ButtonValue.SettingsMenu key) {
        ui_render.setSettingsHover(key);
    }
    public void setSettingsSelected(ButtonValue.SettingsMenu key) {
        ui_render.setSettingsSelected(key);
    }
    public void resetSettingsHover(){
        ui_render.resetSettingsHover();
    }
    public void setGameOverHover(ButtonValue.GameOverMenu key) {
        ui_render.setGameOverHover(key);
    }
    public void setGameOverSelected(ButtonValue.GameOverMenu key) {
        ui_render.setGameOverSelected(key);
    }
    public void resetGameOverHover(){
        ui_render.resetGameOverHover();
    }

    public void setWinHover(ButtonValue.WinMenu key) {
        ui_render.setWinHover(key);
    }
    public void setWinSelected(ButtonValue.WinMenu key) {
        ui_render.setWinSelected(key);
    }
    public void resetWinHover(){
        ui_render.resetWinHover();
    }

}
//-------------------------------------------------------------------------------------------------------------------
