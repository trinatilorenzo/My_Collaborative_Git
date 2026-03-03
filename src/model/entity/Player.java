package model.entity;

import controller.KeyHandler;

import static main.GameSetting.*;

import java.awt.Rectangle;

// - PLAYER CLASS <-- the player entity, with all the player related status and methods
//-------------------------------------------------------------------------------------------------------------------
public class Player extends Entity {

    public final int screenX;
    public final int screenY;

    private int facingRight; //1 for right, -1 for left
    private boolean attacking = false;

    private PlayerState state; // to manage the animation state of the player
    public enum PlayerState {
        IDLE, 
        WALKING, 
        ATTACKING
    }

    public Player(){
        screenX = SCREEN_WIDTH / 2 - TILE_SIZE / 2;
        screenY = SCREEN_HEIGHT / 2 - TILE_SIZE / 2;
        
        solidArea = new Rectangle(76, 88, 40, 40);

        setDefaultValues();
    }

    private void setDefaultValues(){
        worldX = START_WORLD_X; //player's position on the world map
        worldY = START_WORLD_Y;
        speed = PLAYER_SPEED;
        direction = Direction.RIGHT;
        state = PlayerState.IDLE;
        facingRight = 1;
        currentLayer = 3;
    }

    public void update(KeyHandler keyH){
        boolean isMoving = false; // to check if the player is moving, used to set the correct animation state
        super.update(); // reset collision and movement logic
        if (attacking){
            state = PlayerState.ATTACKING;
            return;
        }

        if (keyH.isUp()) {
            direction = Direction.UP;
            isMoving = true;
        } else if (keyH.isDown()) {
            direction = Direction.DOWN;
            isMoving = true;
        } else if (keyH.isLeft()) {
            direction = Direction.LEFT;
            isMoving = true;
            facingRight = -1;
        } else if (keyH.isRight()) {
            direction = Direction.RIGHT;
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
    public PlayerState getState() { return state; }
    public int getFacingRight() { return facingRight; }
    public int getScreenX(){return screenX;}
    public int getScreenY(){return screenY;}
    //---------------------------------

}
//-------------------------------------------------------------------------------------------------
