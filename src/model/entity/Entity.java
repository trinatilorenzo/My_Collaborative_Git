package model.entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {

    public int worldX, worldY;
    public int speed;

    public String direction;

    public BufferedImage warrior;

    public Rectangle solidArea;

    public boolean collisionOn = false;
    

}
