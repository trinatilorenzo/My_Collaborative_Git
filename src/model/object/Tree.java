package model.object;

public class Tree extends GameObject {

    public Tree() {

        name = "Tree";
        try {
            image = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/objects/tree.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
