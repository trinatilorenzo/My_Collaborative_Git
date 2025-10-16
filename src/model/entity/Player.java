package model.entity;



import controller.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {
    private AnimationType currentAnimation;
    final int spriteWidth = 192;
    final int spriteHeight = 192;


    private int gameFrame = 0;
    final int straggerFrames = 5;
    private int facingRight = 0; //0 -> right, 1 -> left

    public enum AnimationType{
        idle(0),
        walk(1),
        attack_right(2),
        attack_down(4),
        attack_up(6);

        public final int row;
        AnimationType(int row){
            this.row = row;
        }
    }

    public Player(){
        setDefaultValues();
        getPlayerSpriteSheet();
        currentAnimation = AnimationType.idle;
    }

    public void setDefaultValues(){
        x = 100;
        y = 100;
        speed = 4;
        direction = "right";
    }

    public void getPlayerSpriteSheet(){
        try {
            warrior = ImageIO.read(getClass().getResourceAsStream("/res/player/Warrior_blue.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(KeyHandler keyH){
        if(keyH.isUp()){
            direction = "up";
            currentAnimation = AnimationType.walk;
            y -= speed;
        }
        else if(keyH.isDown()){
            direction = "down";
            currentAnimation = AnimationType.walk;
            y += speed;
        }
        else if(keyH.isLeft()){
            direction = "left";
            currentAnimation = AnimationType.walk;
            x -= speed;
            facingRight = 1;
        }
        else if(keyH.isRight()){
            direction = "right";
            currentAnimation = AnimationType.walk;
            x += speed;
            facingRight = 0;
        }/*else if (keyH.attackPressed == true){
            if (direction == "right" || direction == "left")
                currentAnimation = AnimationType.attack_right;
            else if (direction == "down")
                currentAnimation = AnimationType.attack_down;
            else if (direction == "up")
                currentAnimation = AnimationType.attack_up;
        }*/else{
            currentAnimation = AnimationType.idle;
        }
    }


    public void attack(){
        // to do
        if (direction == "right" || direction == "left")
            currentAnimation = AnimationType.attack_right;
        else if (direction == "down")
            currentAnimation = AnimationType.attack_down;
        else if (direction == "up")
            currentAnimation = AnimationType.attack_up;
    }

    public void draw(Graphics2D g2){

        BufferedImage image = warrior;

        /*if (gameFrame % straggerFrames == 0){
            if (frameX < 5){
                frameX++;
                System.out.println(frameX);
            }else
                frameX = 0;
        }*/
        int position = (int)Math.floor(gameFrame/straggerFrames) % 6;
        int frameX = spriteWidth * position;
        int frameY = spriteHeight * currentAnimation.row;

        //Check attacco finito


        if (facingRight == 1){
            g2.drawImage(   image,
                    x+spriteWidth, //dx1
                    y, //dy1
                    x, //dx2
                    y+spriteHeight, //dx2
                    frameX, //sx1
                    frameY, //sy1
                    frameX+spriteWidth, //sx2
                    frameY+spriteHeight, //sy2
                    null); //observer

        }else{
            g2.drawImage(   image,
                    x, //dx1
                    y, //dy1
                    x+spriteWidth, //dx2
                    y+spriteHeight, //dx2
                    frameX, //sx1
                    frameY, //sy1
                    frameX+spriteWidth, //sx2
                    frameY+spriteHeight, //sy2
                    null); //observer
        }

        gameFrame++;

    }
}