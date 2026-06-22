package tinyswordsisland.model.util.GameSystem;


import tinyswordsisland.model.GameModel;
import tinyswordsisland.model.object.GameObject;
import tinyswordsisland.model.object.OBJ_PowerUp;
import tinyswordsisland.model.object.OBJ_Tree;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class WorldObjectSystem {

    public void update(GameModel model, double deltaMs) {
        List<GameObject> toSpawn = new ArrayList<>();

        for (GameObject obj : model.getObjects()) {
            if (!obj.isRemoved()) {
                obj.update(deltaMs);

                if (obj instanceof OBJ_Tree tree && tree.shouldDropPowerUp()) {
                    Rectangle treeHitbox = tree.getSolidArea();

                    int powerUpX = tree.getWorldX()
                            + treeHitbox.x
                            + (treeHitbox.width - model.getGameConfig().ObjConfig().POWER_UP_SIZE) / 2;

                    int powerUpY = tree.getWorldY()
                            + treeHitbox.y
                            + (treeHitbox.height - model.getGameConfig().ObjConfig().POWER_UP_SIZE) / 2;

                    toSpawn.add(new OBJ_PowerUp(
                            model.getGameConfig().ObjConfig(),
                            tree.getHiddenPowerUp(),
                            powerUpX,
                            powerUpY,
                            tree.getLayer()
                    ));
                }
            }
        }

        if (!toSpawn.isEmpty()) {
            model.getObjects().addAll(toSpawn);
        }

        model.getObjects().removeIf(GameObject::isRemoved);
    }
}