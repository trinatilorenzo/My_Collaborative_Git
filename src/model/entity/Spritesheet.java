package model.entity;

import java.awt.image.BufferedImage;

// - SPRITESHEET CLASS
//   represent a spritesheet image and allow to crop single sprite from it
//-------------------------------------------------------------------------------------------------------------------
public class Spritesheet {
    private BufferedImage spriteSheet;
    private final int spriteWidth;
    private final int spriteHeight;

    public Spritesheet(BufferedImage spriteSheet, int spriteWidth, int spriteHeight){
        this.spriteSheet = spriteSheet;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }// end constructor

    public BufferedImage getSprite(int x, int y, int width, int height){
        return spriteSheet.getSubimage(x, y, width, height);
    }// end getSprite method

}
//-------------------------------------------------------------------------------------------------------------------

