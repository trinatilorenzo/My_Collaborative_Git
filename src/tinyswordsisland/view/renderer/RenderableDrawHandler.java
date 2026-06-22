package tinyswordsisland.view.renderer;

import tinyswordsisland.model.IRenderable;

import java.awt.Graphics2D;

public interface RenderableDrawHandler<T extends IRenderable> {
    Class<T> getType();

    void draw(Graphics2D g2, T obj, int screenX, int screenY);

    void drawDebug(Graphics2D g2, T obj, int screenX, int screenY, int playerCurrentLayer);

    void update(T obj, double deltaMs);
}
