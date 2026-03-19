package model.collision;

import model.entity.Entity;
import model.GameModel;

import javax.swing.*;

import static main.GameSetting.*;
import static main.GameSetting.Direction.*;

/**
 * The COLLISIONCHEKER CLASS is responsible for determining whether an entity
 * within the game world collides with the game's tilemap, objects, or other entities.
 */
//-------------------------------------------------------------------------------------------------------------------
public class CollisionChecker {

    private final GameModel gameModel;
    private record EntityBounds(int leftX, int rightX, int topY, int bottomY, int layer) {

        static EntityBounds of(Entity entity) {
            // LEFT BORDER of entity hitbox in the world coordinates
            int leftX = entity.getWorldX() + entity.getSolidArea().x;
            // RIGTH BORDER of entity hitbox in the world coordinates
            int rightX = leftX + entity.getSolidArea().width - 1;
            // UPPER BORDER of entity hitbox in the world coordinates
            int topY = entity.getWorldY() + entity.getSolidArea().y;
            // BOTTOM BORDER of entity hitbox in the world coordinates
            int bottomY = topY + entity.getSolidArea().height - 1;
            return new EntityBounds(leftX, rightX, topY, bottomY, entity.getCurrentLayer());
        }
    }

    // COSTRUCTOR
    //-------------------------------------------------------------
    public CollisionChecker(GameModel model) {
        this.gameModel = model;
    }
    //-------------------------------------------------------------


    /**
     * Checks for potential collisions between the given entity and the game world tiles
     */
    //-------------------------------------------------------------
    public void checkTile(Entity entity) {

        EntityBounds bounds = EntityBounds.of(entity);

        checkAxisX(entity, bounds);
        checkAxisY(entity, bounds);
    }
    //-------------------------------------------------------------

    private void checkAxisX(Entity entity, EntityBounds bounds) {
        int dx = entity.getDx();
        if (dx == 0) return; // not moving

        //anticipate the entity movement (move left or right)
        int projectedLeftCol = (bounds.leftX + dx) / TILE_SIZE;
        int projectedRightCol = (bounds.rightX + dx) / TILE_SIZE;
        int rowTop = bounds.topY / TILE_SIZE;
        int rowBottom = bounds.bottomY / TILE_SIZE;

        int checkCol;
        if (dx < 0) {
            //moving left --> check left column
            checkCol = projectedLeftCol;
        } else {
            //moving right --> check right column
            checkCol = projectedRightCol;
        }

        if (isCollision(bounds.layer, rowTop, checkCol) || isCollision(bounds.layer, rowBottom, checkCol)) {
                entity.setCollisionX(true);
        }




    }

    private void checkAxisY(Entity entity, EntityBounds bounds) {
        int dy = entity.getDy();
        if (dy == 0) return; // not moving

        //anticipate the entity movement (move up or down)
        int colLeft = bounds.leftX / TILE_SIZE;
        int colRight = bounds.rightX / TILE_SIZE;
        int projectedTopRow = (bounds.topY + dy) / TILE_SIZE;
        int projectedBottomRow = (bounds.bottomY + dy) / TILE_SIZE;


        int checkRow;
        if (dy < 0) {
            //moving up --> check the top row
            checkRow = projectedTopRow;
        }else{
            //moving down --> check the bottom row
            checkRow = projectedBottomRow;
        }

        if (isCollision(bounds.layer, checkRow, colLeft) || isCollision(bounds.layer, checkRow, colRight)) {

            updateEntityLayer(entity, bounds, checkRow, colLeft);

            entity.setCollisionY(true);


        }
    }

    //-------------------------------------------------------------


    public void updateEntityLayer(Entity entity, EntityBounds bounds, int checkRow, int colLeft){
        if (entity.getDx()!=0) return; // Only update layer on vertical movement
        if (!isCollision(bounds.layer - 1, checkRow, colLeft) && entity.getDirection() != UP) {
            // move level down
            entity.setLayer(entity.getCurrentLayer() - 1);
            System.out.println("Moved down to layer " + entity.getCurrentLayer());
        }
        if (!isCollision(bounds.layer + 1, checkRow, colLeft)&& entity.getDirection() != DOWN) {
            // move level up
            entity.setLayer(entity.getCurrentLayer() + 1);
            System.out.println("Moved up to layer " + entity.getCurrentLayer());
        }
    }
    //-------------------------------------------------------------

    /**
     * Checks if a collision occurs at the specified layer, row, and column
     * within the game world.
     */
    //-------------------------------------------------------------
    private boolean isCollision(int layer, int row, int col) {
        if (isOutOfBounds(row, col)) {
            return true;
        }
        return gameModel.getWorldMap().hasCollision(layer, row, col);
    }
    //-------------------------------------------------------------

    /**
     * check if the entity is out of the map
     */

    //-------------------------------------------------------------
    private boolean isOutOfBounds(int row, int col) {
        return row < 0 || col < 0
                || row >= gameModel.getWorldMap().getMaxMapRow()
                || col >= gameModel.getWorldMap().getMaxMapCol();
    }
    //-------------------------------------------------------------




}
//-------------------------------------------------------------------------------------------------------------------