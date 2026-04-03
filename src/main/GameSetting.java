package main;
/*
import model.GameModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.io.File;


public class GameSetting {

    // ASSETT LOCATION
    //-----------------------------------------------------------------------
    public static final String MAP_PATH = "/res/maps/MappaGiocoV4.tmx";
    public static final String TILESET_PATH = "/res/tiles/tileSet1.png"; //TODO form xml
    //-----------------------------------------------------------------------
    // FPS
    //----------------------------------------------------------------------
    public static final int FPS = 120;
    public static final int MAX_FRAME_SKIP = 10;
    //----------------------------------------------------------------------
    // SCREEN SETTINGS
    //-----------------------------------------------------------------------
    //TODO ridimentionamento automatico
    public final int ORIGINAL_TILE_SIZE;
    public static final int SCALE = 1;
    public final int TILE_SIZE;
    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;
    public final int SCREEN_WIDTH;
    public final int SCREEN_HEIGHT;
    //-----------------------------------------------------------------------
    // WORLD SETTINGS
    //-----------------------------------------------------------------------
    public final int MAX_WORLD_COL;
    public final int MAX_WORLD_ROW;
    public static final int GRAPHIC_LAYER_NUM = 9; //TODO form xml
    public static final int GAME_LAYER_NUM = 4; //TODO form xml
    public final Color GAME_BG_COLOR;
    //-----------------------------------------------------------------------
    //TILESET
    //-----------------------------------------------------------------------
    public static final int MAX_TILESET_RAW = 40; //TODO form xml
    public static final int MAX_TILESET_COL = 20; //TODO form xml
    public static final int TILESNUM = (MAX_TILESET_COL*MAX_TILESET_RAW); //TODO form xml
    //-----------------------------------------------------------------------
    // PLAYER SETTINGS
    //-----------------------------------------------------------------------
    public static final int START_PLAYER_SPEED = 6*60; // pixel per second
    //position
    public final int START_WORLD_X;
    public final int START_WORLD_Y;
    public static final int START_WORLD_LAYER = 3;
    //render
    public static final int PLAYER_SPRITE_WIDTH =  192;
    public static final int PLAYER_SPRITE_HEIGHT =  192;
    public static final int PLAYER_SCALE= 1;
    public static final int PLAYER_RENDER_WIDTH = (PLAYER_SPRITE_WIDTH * PLAYER_SCALE);
    public static final int PLAYER_RENDER_HEIGHT = (PLAYER_SPRITE_HEIGHT * PLAYER_SCALE);
    public static final int FACING_RIGHT = 1;
    public static final int FACING_LEFT = -1;
    //hitbox
    public static final int PLAYER_HITBOX_WIDTH = (45 * PLAYER_SCALE);
    public static final int PLAYER_HITBOX_HEIGHT = (35 * PLAYER_SCALE);

    //-----------------------------------------------------------------------
    // UI SETTINGS
    //-----------------------------------------------------------------------

    //-----------------------------------------------------------------------
    //OBJ
    //-----------------------------------------------------------------------
    public static final int TREE_SPRITE_WIDTH = 192; //TODO form xml
    public static final int TREE_SPRITE_HEIGHT = 256; //TODO form xml
    public static final int TREE_HITBOX_WIDTH = 40;
    public static final int TREE_HITBOX_HEIGHT = 25;
    public static final int TREE_HEALTH = 4;
    // Duration (ms) of each frame for tree idle animation
    public static final double TREE_IDLE_FRAME_MS = 100.0; //TODO form xml

    //-----------------------------------------------------------------------
    // GAME STATE
    //-----------------------------------------------------------------------
    public enum GameState {PLAYING, PAUSED}
    //-----------------------------------------------------------------------
    // Direction
    //-----------------------------------------------------------------------
    public enum Direction {UP, DOWN, LEFT, RIGHT}
    //-----------------------------------------------------------------------
    // Player State
    //-----------------------------------------------------------------------
    public enum PlayerState {IDLE, WALKING, ATTACKING}
    //-----------------------------------------------------------------------




    public GameSetting() {

        Document mapDoc = loadMapDoc(MAP_PATH);

        // SCREEN SETTINGS
        //-----------------------------------------------------------------------
        assert mapDoc != null;
        this.ORIGINAL_TILE_SIZE = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("tilewidth"));
        this.TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;

        this.SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
        this.SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

        this.MAX_WORLD_COL = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("width"));
        this.MAX_WORLD_ROW = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("height"));
        this.GAME_BG_COLOR = Color.decode(mapDoc.getDocumentElement().getAttribute("backgroundcolor"));

        this.START_WORLD_X = 62 * TILE_SIZE;
        this.START_WORLD_Y = 19 * TILE_SIZE;

    }

    private Document loadMapDoc(String mapPath){
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(mapPath));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            System.err.println("Errore nel parsing del TMX: " + e.getMessage());
        }
        return null;
    }


}
*/