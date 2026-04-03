package main.CONFIG;

import org.w3c.dom.Document;
import java.awt.Color;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * GameConfig
 */
//----------------------------------------------------------------------------------------------------------------------


public final class GameConfig {

    public static final String MAP_PATH = "res/maps/MappaGiocoV4.tmx";
    public static final String TILESET_PATH = "/res/tiles/tileSet1.png";

    public static final int FPS =120;
    public static final int MAX_FRAME_SKIP = 10;
    public static final int SCALE = 1;
    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;

    private final ScreenConfig screenConfig;
    private final MapConfig mapConfig;
    private final EntityConfig entityConfig;
    private final ObjConfig ObjConfig;

    private Document mapDoc;

    public GameConfig() {
        this.mapDoc = loadMapDoc(MAP_PATH);

        if (mapDoc == null) {
            throw new IllegalStateException("Impossibile caricare la mappa: " + MAP_PATH);
        }

        int TILE_SIZE = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("tilewidth"));
        int MAX_WORLD_COL = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("width"));
        int MAX_WORLD_ROW = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("height"));
        Color GAME_BG_COLOR = Color.decode(mapDoc.getDocumentElement().getAttribute("backgroundcolor"));

        this.screenConfig = new ScreenConfig(TILE_SIZE, SCALE, MAX_SCREEN_COL, MAX_SCREEN_ROW, GAME_BG_COLOR);
        this.mapConfig = new MapConfig(TILE_SIZE, MAX_WORLD_COL, MAX_WORLD_ROW);
        this.entityConfig = new EntityConfig(screenConfig);
        this.ObjConfig = new ObjConfig();
    }

    private Document loadMapDoc(String mapPath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(mapPath)) {
            if (is == null) {
                throw new IllegalArgumentException("Risorsa non trovata: " + mapPath);
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Errore nel parsing del TMX", e);
        }
    }

    public ScreenConfig screenConfig() {
        return screenConfig;
    }

    public MapConfig mapConfig() {
        return mapConfig;
    }

    public EntityConfig entityConfig() {
        return entityConfig;
    }

    public ObjConfig ObjConfig() {
        return ObjConfig;
    }

    public Document mapDoc(){return mapDoc;};
}
//----------------------------------------------------------------------------------------------------------------------
