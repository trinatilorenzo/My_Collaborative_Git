package main.CONFIG;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
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

    public static final int FPS = 120;
    public static final int MAX_FRAME_SKIP = 10;
    public static final int SCALE = 1;
    public static final int MAX_SCREEN_COL = 20;
    public static final int MAX_SCREEN_ROW = 12;

    private static final String ENTITY_GROUP_NAME = "entity";
    private static final String PLAYER_NAME = "player";
    private static final String MONK_NAME = "monk";
    private static final String START_LAYER_PROP = "StartLayer";

    //LAYER1 COLLISON FOR BRIDGE x : 42, 43, 44 y:25
    //LAYER2 COLLISON FOR BRIDGE x : 57, 58, 59 y:43


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

        Map<String, SpawnInfo> entitySpawns = loadEntitySpawns(mapDoc);

        int defaultPlayerX = 62 * TILE_SIZE;
        int defaultPlayerY = 19 * TILE_SIZE;
        int defaultPlayerLayer = 3;
        int defaultMonkX = 62 * TILE_SIZE;
        int defaultMonkY = 18 * TILE_SIZE;
        int defaultMonkLayer = 3;

        SpawnInfo playerSpawn = entitySpawns.getOrDefault(PLAYER_NAME,
                new SpawnInfo(defaultPlayerX, defaultPlayerY, defaultPlayerLayer));
        SpawnInfo monkSpawn = entitySpawns.getOrDefault(MONK_NAME,
                new SpawnInfo(defaultMonkX, defaultMonkY, defaultMonkLayer));


        this.screenConfig = new ScreenConfig(TILE_SIZE, SCALE, MAX_SCREEN_COL, MAX_SCREEN_ROW, GAME_BG_COLOR);
        this.mapConfig = new MapConfig(TILE_SIZE, MAX_WORLD_COL, MAX_WORLD_ROW);
        this.entityConfig = new EntityConfig(
                screenConfig,
                playerSpawn.x(),
                playerSpawn.y(),
                playerSpawn.layer(),
                monkSpawn.x(),
                monkSpawn.y(),
                monkSpawn.layer()
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

    //TODO rivedere (fatto tutto da AI funziona ma non so pereche)

    private Map<String, SpawnInfo> loadEntitySpawns(Document document) {
        Map<String, SpawnInfo> positions = new HashMap<>();

        NodeList objects = document.getElementsByTagName("object");
        for (int i = 0; i < objects.getLength(); i++) {
            Node node = objects.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element obj = (Element) node;
            Node parent = obj.getParentNode();
            if (parent == null || parent.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element parentElement = (Element) parent;
            if (!ENTITY_GROUP_NAME.equalsIgnoreCase(parentElement.getAttribute("name"))) {
                continue;
            }

            String name = obj.getAttribute("name");
            if (name == null || name.isBlank()) {
                continue;
            }

            // TMX point coordinates mark the intended spawn point (center of sprite). Convert to sprite top-left.
            double rawX = Double.parseDouble(obj.getAttribute("x"));
            double rawY = Double.parseDouble(obj.getAttribute("y"));
            int x = (int) Math.round(rawX - (EntityConfig.SPRITE_WIDTH / 2.0));
            int y = (int) Math.round(rawY - (EntityConfig.SPRITE_HEIGHT / 2.0));

            int layer = EntityConfig.START_WORLD_LAYER; // fallback
            NodeList props = obj.getElementsByTagName("property");
            for (int p = 0; p < props.getLength(); p++) {
                Node propNode = props.item(p);
                if (propNode.getNodeType() != Node.ELEMENT_NODE) continue;
                Element propEl = (Element) propNode;
                if (START_LAYER_PROP.equalsIgnoreCase(propEl.getAttribute("name"))) {
                    try {
                        layer = Integer.parseInt(propEl.getAttribute("value"));
                    } catch (NumberFormatException ignored) {
                        // keep fallback
                    }
                }
            }

            positions.put(name.toLowerCase(), new SpawnInfo(x, y, layer));
        }

        return positions;
    }

    private record SpawnInfo(int x, int y, int layer) {}

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
