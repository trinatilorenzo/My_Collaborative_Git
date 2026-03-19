package model.object;

import java.util.ArrayList;
import java.util.List;
import model.object.GameObject;

public class WorldManager {

    private final List<GameObject> objects = new ArrayList<>();

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

}