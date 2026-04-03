package model.object;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static main.GameSetting.TILE_SIZE;

public class ObjectManager {

    private final List<GameObject> objects = new ArrayList<>();

    public ObjectManager(){
        // Carica gli oggetti dal file TMX fornito; se fallisce usa il bootstrap statico
        addOBJFromFile("src/res/maps/MappaGiocoV4.tmx");
        //if (objects.isEmpty()) {
           // spawnStaticObjects();
       // }
    }

    public void add(GameObject obj){
        objects.add(obj);
    }

    public List<GameObject> getObjects(){
        return objects;
    }

    public void update(double deltaMs){

        objects.removeIf(GameObject::isRemoved);

        for(GameObject obj : objects){
            obj.update(deltaMs);
        }
    }


    public void addOBJFromFile(String path){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(path));
            doc.getDocumentElement().normalize();

            NodeList groups = doc.getElementsByTagName("objectgroup");
            for (int i = 0; i < groups.getLength(); i++) {
                Element group = (Element) groups.item(i);
                if (!"trees".equals(group.getAttribute("name"))) {
                    continue;
                }

                NodeList objects = group.getElementsByTagName("object");
                for (int j = 0; j < objects.getLength(); j++) {
                    Node node = objects.item(j);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    Element obj = (Element) node;
                    int gid = Integer.parseInt(obj.getAttribute("gid"));
                    double x = Double.parseDouble(obj.getAttribute("x"));
                    double y = Double.parseDouble(obj.getAttribute("y"));
                    double height = Double.parseDouble(obj.getAttribute("height"));

                    int worldX = (int) Math.round(x);
                    int worldY = (int) Math.round(y - height); // Tiled usa ancoraggio sul piede

                    //if (gid == 801) {
                        add(new OBJ_Tree(worldX, worldY));
                    //}
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel parsing del TMX: " + e.getMessage());
        }
    }


}
