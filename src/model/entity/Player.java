package model.entity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import controller.KeyHandler;
import static main.GameSetting.*;

// - PLAYER CLASS
// represent the player entity in the game
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity{
    private final int spriteWidth = 192;
    private final int spriteHeight = 192;
    private int  facingRight = 1; //1 for right, -1 for left
    
    private AnimationManager animationManager;
    private Spritesheet spriteSheet;

    private boolean isAttacking = false;

    public final int screenX;
    public final int screenY;

    public Player(){
        
        screenX = SCREEN_WIDTH / 2 - spriteWidth / 2;
        screenY = SCREEN_HEIGHT / 2 - spriteHeight / 2;
        setDefaultValues();
        loadSpriteSheet();
    }

    private void setDefaultValues(){
        worldX = 25; //player's position on the world map
        worldY = 25; //DOPO DOVRO METTERE TILE_SIZE * un certo numero in base a dove voglio farlo partire
        speed = 4;
        direction = "right";
    }

    private void loadSpriteSheet(){
        try {
            BufferedImage sheetImage = ImageIO.read(getClass().getResourceAsStream("/res/player/Warrior_blue.png"));
            spriteSheet = new Spritesheet(sheetImage, spriteWidth, spriteHeight);

            animationManager = new AnimationManager();
            animationManager.addAnimation("idle", createAnimation(0, 1, 6, 6, true));
            animationManager.addAnimation("walk", createAnimation(1, 1, 6, 5, true));
            animationManager.addAnimation("attack_right", createAnimation(2, 2, 6 , 4, false));
            animationManager.addAnimation("attack_down", createAnimation(4, 2,  6, 4, false));
            animationManager.addAnimation("attack_up", createAnimation(6,2, 6, 4, false));
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }// end loadSpriteSheet method

    private Animation createAnimation(int startRow, int rows, int cols, int frameDuration, boolean loop){
        BufferedImage[] frames = new BufferedImage[rows*cols]; // array to hold the frames of the animation
        int index = 0;
        for(int i = 0; i < rows; i++){
            for (int j = 0; j<cols; j++){
                frames[index++] = spriteSheet.getSprite(
                    j * spriteWidth, 
                    (startRow+i) * spriteHeight, 
                    spriteWidth, 
                    spriteHeight);
            }
        }
        return new Animation(frames, frameDuration, loop);
    }

    public void update(KeyHandler keyH){

        if (isAttacking){
            animationManager.update();

            if (animationManager.getCurrent().isFinished()){
                isAttacking = false;
                animationManager.playAnimation("idle");
            }
            return;
        }

        boolean isMoving = false;
        // Movement logic
        if (keyH.isUp()){
            direction = "up";
            worldY -= speed;
            isMoving = true;
        }
        else if (keyH.isDown()){
            direction = "down";
            worldY += speed;
            isMoving = true;
        }
        else if (keyH.isLeft()){
            direction = "left";
            worldX -= speed;
            facingRight = -1;
            isMoving = true;
        }
        else if (keyH.isRight()){
            direction = "right";
            worldX += speed;
            facingRight = 1;
            isMoving = true;
        }
        //Attack animation
        if (keyH.isAttack() && !isAttacking){
            isAttacking = true;
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

    public int getWorldX(){
        return worldX;
    }

    public int getWorldY(){
        return worldY;
    }

    public int getScreenX(){
        return screenX;
    }

    public int getScreenY(){
        return screenY;
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
