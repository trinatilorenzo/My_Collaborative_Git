package view.renderer.object;

import model.object.GameObject;

import java.awt.Graphics2D;
/**
 * The OBJECT RENDER CLASS is responsible for rendering the visual representation of the game objects, such as trees, etc.
 */
//-------------------------------------------------------------------------------------------------------------------
public abstract class ObjectRender<T extends GameObject> {
//TODO renderla utile
    public abstract void update(T obj, double deltaMs);

    public abstract void draw(Graphics2D g2, T obj, int screenX, int screenY);

}
