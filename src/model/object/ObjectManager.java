package model.object;


import java.util.ArrayList;
import java.util.List;
import main.CONFIG.ObjConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

<<<<<<< HEAD
=======
//TODO sintassi commenti

>>>>>>> 069bd5f6f2c1b577b57e86fb611131d32e5b3c56
public class ObjectManager {

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

                //TODO migliorare caricmeto degli oggetti
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
                        add(new OBJ_Tree(worldX, worldY, objConfig));
                    //}
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel parsing del TMX: " + e.getMessage());
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

}
