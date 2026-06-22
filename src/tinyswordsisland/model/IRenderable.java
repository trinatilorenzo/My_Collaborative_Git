package tinyswordsisland.model;
import java.awt.Rectangle;

public interface IRenderable {
    int getWorldX();
    int getWorldY();
    Rectangle getSolidArea();
    int getWidth();
    int getHeight();
}
