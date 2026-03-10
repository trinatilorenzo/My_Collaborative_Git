package model.object;

public class OBJ_Tree extends GameObject {

    public OBJ_Tree() {

        name = "OBJ_Tree";
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/tree.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
