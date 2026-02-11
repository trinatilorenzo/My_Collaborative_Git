package model.entity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import controller.KeyHandler;

// - PLAYER CLASS
// represent the player entity in the game
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity{
    private final int spriteWidth = 192;
    private final int spriteHeight = 192;
    private int  facingRight = 1; //1 for right, -1 for left
    
    private AnimationManager animationManager;
    private Spritesheet spriteSheet;

    public Player(){
        setDefaultValues();
        loadSpriteSheet();

    }

    private void setDefaultValues(){
        x = 100;
        y = 100;
        speed = 4;
        direction = "right";
    }

    private void loadSpriteSheet(){
        try {
            BufferedImage sheetImage = ImageIO.read(getClass().getResourceAsStream("/res/player/Warrior_blue.png"));
            spriteSheet = new Spritesheet(sheetImage, spriteWidth, spriteHeight);

            animationManager = new AnimationManager();
            animationManager.addAnimation("idle", createAnimation(0, 6, 6));
            animationManager.addAnimation("walk", createAnimation(1, 6, 5));
            animationManager.addAnimation("attack_right", createAnimation(2, 6, 4));
            animationManager.addAnimation("attack_down", createAnimation(4, 6, 4));
            animationManager.addAnimation("attack_up", createAnimation(6, 6, 4));
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// end loadSpriteSheet method

    private Animation createAnimation(int row, int frameCount, int frameDuration){
        BufferedImage[] frames = new BufferedImage[frameCount]; // array to hold the frames of the animation
        for(int i = 0; i < frameCount; i++){
            frames[i] = spriteSheet.getSprite(i * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
        }
        return new Animation(frames, frameDuration);
    }

    public void update(KeyHandler keyH){
        boolean isMoving = false;
        // Movement logic
        if (keyH.isUp()){
            direction = "up";
            y -= speed;
            isMoving = true;
        }
        else if (keyH.isDown()){
            direction = "down";
            y += speed;
            isMoving = true;
        }
        else if (keyH.isLeft()){
            direction = "left";
            x -= speed;
            facingRight = -1;
            isMoving = true;
        }
        else if (keyH.isRight()){
            direction = "right";
            x += speed;
            facingRight = 1;
            isMoving = true;
        }
        //Attack animation
        if (keyH.isAttack()){
            if (direction.equals("right")|| direction.equals("left")){
                animationManager.playAnimation("attack_right");
            } else if (direction.equals("down")){ 
                animationManager.playAnimation("attack_down");
            } else {
                animationManager.playAnimation("attack_up");
            }
        }
        else if (isMoving){
            animationManager.playAnimation("walk");
        } else {
            animationManager.playAnimation("idle");
        }

        animationManager.update();
    }
        
    // GETTERS
    
    public int getSpriteWidth(){
        return spriteWidth;
    }

    public int getSpriteHeight(){
        return spriteHeight;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
    public int getFacingRight(){
        return facingRight;
    }

    public BufferedImage getCurrentFrame(){
            return animationManager.getCurrent().getCurrentFrame();
            //get the current animation and then get its current frame
    }

}// end PLAYER CLASS
//-------------------------------------------------------------------------------------------------------------------
