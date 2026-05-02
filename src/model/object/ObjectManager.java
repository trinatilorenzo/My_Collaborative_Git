package model.object;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.CONFIG.ObjConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//TODO rimuovere

public class ObjectManager {

    private static final String TREES_GROUP = "trees";
    private static final String STRUCTURES_GROUP = "buildings";
    private static final int TMX_GID_MASK = 0x1FFFFFFF;

    private static final Map<Integer, String> STRUCTURE_SPRITE_BY_GID = new HashMap<>();
    static {
        STRUCTURE_SPRITE_BY_GID.put(802, "/res/object/buildings/Castle_Blue.png");
        STRUCTURE_SPRITE_BY_GID.put(803, "/res/object/buildings/Tower_Blue.png");
        STRUCTURE_SPRITE_BY_GID.put(804, "/res/object/buildings/GoldMine_Active.png");
    }

    private final List<GameObject> objects = new ArrayList<>();
    ObjConfig objConfig;

    public ObjectManager(ObjConfig objConfig, Document mapDoc){
        this.objConfig = objConfig;
        addOBJFromFile(mapDoc);
    }

    public void add(GameObject obj){
        objects.add(obj);
    }

    public void addOBJFromFile(Document mapDoc){
        try {

            NodeList groups = mapDoc.getElementsByTagName(objConfig.OBJ_TAG);

            for (int i = 0; i < groups.getLength(); i++) {
                Element group = (Element) groups.item(i);
                String groupName = group.getAttribute("name");

                if (TREES_GROUP.equals(groupName)) {
                    loadTrees(group);
                } else if (STRUCTURES_GROUP.equals(groupName)) {
                    loadStructures(group);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel parsing del TMX: " + e.getMessage());
        }
    }

    private void loadTrees(Element group) {
        NodeList objects = group.getElementsByTagName("object");
        for (int j = 0; j < objects.getLength(); j++) {
            Node node = objects.item(j);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element obj = (Element) node;
            if (!obj.hasAttribute("gid")) continue;

            double x = Double.parseDouble(obj.getAttribute("x"));
            double y = Double.parseDouble(obj.getAttribute("y"));
            double height = Double.parseDouble(obj.getAttribute("height"));

            int worldX = (int) Math.round(x);
            int worldY = (int) Math.round(y - height); // Tiled usa ancoraggio sul piede

            add(new OBJ_Tree(worldX, worldY, objConfig));
        }
    }

    private void loadStructures(Element group) {
        NodeList objects = group.getElementsByTagName("object");
        for (int j = 0; j < objects.getLength(); j++) {
            Node node = objects.item(j);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element obj = (Element) node;
            if (!obj.hasAttribute("gid")) continue;

            int rawGid = Integer.parseInt(obj.getAttribute("gid"));
            int gid = rawGid & TMX_GID_MASK; // remove Tiled flip/rotation flags
            String spritePath = STRUCTURE_SPRITE_BY_GID.get(gid);
            if (spritePath == null) continue;

            int width = (int) Math.round(Double.parseDouble(obj.getAttribute("width")));
            int height = (int) Math.round(Double.parseDouble(obj.getAttribute("height")));

            double x = Double.parseDouble(obj.getAttribute("x"));
            double y = Double.parseDouble(obj.getAttribute("y"));

            int worldX = (int) Math.round(x);
            int worldY = (int) Math.round(y - height); // Tiled usa ancoraggio sul piede

            add(new OBJ_Structure(worldX, worldY, width, height, spritePath));
        }
    }

    public void update(double deltaMs){

        objects.removeIf(GameObject::isRemoved);

        for(GameObject obj : objects){
            obj.update(deltaMs);
        }
    }
    public List<GameObject> getObjects(){
        return objects;
    }

    public void reset(Document mapDoc) {
        objects.clear();
        addOBJFromFile(mapDoc);
    }

}
