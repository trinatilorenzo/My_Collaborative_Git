package main.CONFIG;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * GameConfig
 * this class is used to load the game configuration from the XML file or static constants
 */
//----------------------------------------------------------------------------------------------------------------------
public final class GameConfig {

    //RESOURCES PATH
    public static final String MAP_PATH = "res/maps/MappaGiocoV4.tmx";
    public static final String TILESET_PATH = "/res/tiles/tileSet1.png";
    //-------------------------------------------------------------

    //STRING TAG
    private static final String ENTITY_GROUP_NAME = "entity";
    private static final String PLAYER_NAME = "player";
    private static final String MONK_NAME = "monk";
    private static final String TNT_NAME = "TNT_spawnPoint";
    private static final String START_LAYER_PROP = "StartLayer";
    //-------------------------------------------------------------

    public static final int FPS = 120;
    public static final int MAX_FRAME_SKIP = 10;
    public static final int SCALE = 1;
    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;

    // CONFIG
    private final ScreenConfig screenConfig;
    private final MapConfig mapConfig;
    private final EntityConfig entityConfig;
    private final ObjConfig ObjConfig;
    //-------------------------------------------------------------

    //UTIL
    private Document mapDoc;
    //-------------------------------------------------------------

    public GameConfig() {
        this.mapDoc = loadMapDoc(MAP_PATH);

        if (mapDoc == null) {
            throw new IllegalStateException("Impossibile caricare la mappa: " + MAP_PATH);
        }

        int TILE_SIZE = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("tilewidth"));
        int MAX_WORLD_COL = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("width"));
        int MAX_WORLD_ROW = Integer.parseInt(mapDoc.getDocumentElement().getAttribute("height"));
        Color GAME_BG_COLOR = Color.decode(mapDoc.getDocumentElement().getAttribute("backgroundcolor"));



        SpawnPoint playerSpawn = loadEntitySpawns(PLAYER_NAME).get(0);
        SpawnPoint monkSpawn = loadEntitySpawns(MONK_NAME).get(0);
        ArrayList<SpawnPoint> tntSpawn = loadEntitySpawns(TNT_NAME);




        this.screenConfig = new ScreenConfig(TILE_SIZE, SCALE, MAX_SCREEN_COL, MAX_SCREEN_ROW, GAME_BG_COLOR);
        this.mapConfig = new MapConfig(TILE_SIZE, MAX_WORLD_COL, MAX_WORLD_ROW);
        this.entityConfig = new EntityConfig(
                screenConfig,
                loadEntitySpawns(PLAYER_NAME).get(0),
                loadEntitySpawns(MONK_NAME).get(0),
                loadEntitySpawns(TNT_NAME)
        );
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


    //-------------------------------------------------------------
    private ArrayList<SpawnPoint> loadEntitySpawns(String EntityName) {

        ArrayList<SpawnPoint> spawns = new ArrayList<>();
        NodeList groups = mapDoc.getElementsByTagName("objectgroup");

        for (int g = 0; g < groups.getLength(); g++) {
            Node groupNode = groups.item(g);

            if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                Element group = (Element) groupNode;

                if ("entity".equalsIgnoreCase(group.getAttribute("name"))) {
                    NodeList objects = group.getElementsByTagName("object");

                    for (int i = 0; i < objects.getLength(); i++) {
                        Element obj = (Element) objects.item(i);
                        if (EntityName.equalsIgnoreCase(obj.getAttribute("name"))){

                            int x = (int) Math.round(Double.parseDouble(obj.getAttribute("x")));
                            int y = (int) Math.round(Double.parseDouble(obj.getAttribute("y")));
                            int startLayer = Integer.parseInt(getPropertyValue(obj, "StartLayer"));


                            spawns.add(new SpawnPoint(x,y,startLayer));

                        }
                    }
                }
            }
        }

        return spawns;
    }

    private static String getPropertyValue(Element objectElement, String propertyName) {
        NodeList properties = objectElement.getElementsByTagName("property");

        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            String currentPropertyName = property.getAttribute("name");

            if (propertyName.equalsIgnoreCase(currentPropertyName)) {
                return property.getAttribute("value");
            }
        }

        return "";
    }

    //-------------------------------------------------------------

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
