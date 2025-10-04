package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;

public class Player extends Entity{

    GamePanel gp;
    KeyHandler keyH;

    public Player(GamePanel gp, KeyHandler keyH){
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues(){
        x = 100;
        y = 100;
        speed = 4;
        direction = "right";
    }

    public void getPlayerImage(){
        try {
            warrior = ImageIO.read(getClass().getResourceAsStream("/res/player/Warrior_blue.png"));
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(){
        if(keyH.upPressed == true){
            direction = "up";
            y -= speed;
        }
        else if(keyH.downPressed == true){
            direction = "down";
            y += speed;
        }
        else if(keyH.leftPressed == true){
            direction = "left";
            x -= speed;
        }
        else if(keyH.rightPressed == true){
            direction = "right";
            x += speed;
        }
    }

    final int spriteWidth = 192;
    final int spriteHeight = 192;
    int frameX = 0;
    int frameY = 1;
    int gameFrame = 0;
    final int straggerFrames = 5;

    public void draw(Graphics2D g2){
        //g2.setColor(Color.white);
        //g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        BufferedImage image = warrior;
        
        g2.drawImage(   image, 
                        x, //dx1
                        y, //dy1
                        x+spriteWidth, //dx2
                        y+spriteHeight, //dx2
                        frameX*spriteWidth, //sx1
                        frameY*spriteHeight, //sy1
                        (frameX+1)*spriteWidth, //sx2
                        (frameY+1)*spriteHeight, //sy2
                        null); //observer
        
        if (gameFrame % straggerFrames == 0){
            if (frameX < 5){
                frameX++;
                System.out.println(frameX);
            }else 
                frameX = 0;
        }

        gameFrame++;
        
    }
}
    

