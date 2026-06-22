package tinyswordsisland.model;

import tinyswordsisland.model.object.GameObject;

import java.util.List;

public interface CollisionWorld {
    int getTileSize();
    GameMap getWorldMap();
    List<GameObject> getObjects();
}
