package model.entity;

import controller.KeyHandler;
import model.enums.Direction;
import model.enums.PlayerState;

import static main.GameSetting.*;

import java.awt.Rectangle;

public class Player extends Entity {

    private Direction direction;
    private PlayerState state;

    private boolean attacking = false;

    public final int screenX;
    public final int screenY;

    private int worldX;
    private int worldY;
    private int speed;
    private int facingRight; //1 for right, -1 for left

    public Player(){
        screenX = SCREEN_WIDTH / 2 - TILE_SIZE / 2;
        screenY = SCREEN_HEIGHT / 2 - TILE_SIZE / 2;
        
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 15;
        solidArea.width = TILE_SIZE;
        solidArea.height = TILE_SIZE;
        setDefaultValues();
    }

    private void setDefaultValues(){
        worldX = START_WORLD_X; //player's position on the world map
        worldY = START_WORLD_Y;
        speed = PLAYER_SPEED;
        direction = Direction.RIGHT;
        state = PlayerState.IDLE;
        facingRight = 1;
    }

    public void update(KeyHandler keyH){
        boolean isMoving = false; // to check if the player is moving, used to set the correct animation state

        if (attacking){
            state = PlayerState.ATTACKING;
            return;
        }

        if (keyH.isUp()) {
            direction = Direction.UP;
            worldY -= speed;
            isMoving = true;
        } else if (keyH.isDown()) {
            direction = Direction.DOWN;
            worldY += speed;
            isMoving = true;
        } else if (keyH.isLeft()) {
            direction = Direction.LEFT;
            worldX -= speed;
            isMoving = true;
            facingRight = -1;
        } else if (keyH.isRight()) {
            direction = Direction.RIGHT;
            worldX += speed;
            isMoving = true;
            facingRight = 1;
        }

        if (keyH.isAttack() && !attacking) {
            attacking = true;
            state = PlayerState.ATTACKING;
        } else if (isMoving) {
            state = PlayerState.WALKING;
        } else {
            state = PlayerState.IDLE;
        }
    }
    
    public void stopAttack() {
        attacking = false;
        state = PlayerState.IDLE;
    }

    // GETTER ----------------------
    public Direction getDirection() { return direction; }
    public PlayerState getState() { return state; }
    public int getFacingRight() { return facingRight; }
    public int getScreenX(){return screenX;}
    public int getScreenY(){return screenY;}
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    //---------------------------------

}
