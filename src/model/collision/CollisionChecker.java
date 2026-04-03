package model.collision;

import model.entity.Entity;
import model.GameModel;
import model.object.GameObject;

import java.awt.Rectangle;
import java.util.List;

import static main.ENUM.Direction.*;


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

    /**
     * Checks collisions between an entity and solid objects in the world.
     * Collision flags are updated per-axis to match tile handling.
     */
    //-------------------------------------------------------------
    public void checkObjects(Entity entity) {
        List<GameObject> objects = gameModel.getObjects();
        if (objects == null || objects.isEmpty()) return;

        EntityBounds bounds = EntityBounds.of(entity);
        int dx = entity.getDx();
        int dy = entity.getDy();

        for (GameObject obj : objects) {
            if (obj == null || obj.isRemoved() || !obj.isSolid()) continue;
            Rectangle solidArea = obj.getSolidArea();
            if (solidArea == null) continue;

            int objLeft = obj.getWorldX() + solidArea.x;
            int objRight = objLeft + solidArea.width - 1;
            int objTop = obj.getWorldY() + solidArea.y;
            int objBottom = objTop + solidArea.height - 1;

            if (dx != 0 && overlaps(bounds.leftX + dx, bounds.rightX + dx, bounds.topY, bounds.bottomY,
                    objLeft, objRight, objTop, objBottom)) {
                entity.setCollisionX(true);
            }

            if (dy != 0 && overlaps(bounds.leftX, bounds.rightX, bounds.topY + dy, bounds.bottomY + dy,
                    objLeft, objRight, objTop, objBottom)) {
                entity.setCollisionY(true);
            }

            if (entity.isCollisionX() && entity.isCollisionY()) {
                break; // both axes blocked; further checks unnecessary
            }
        }
    }
    //-------------------------------------------------------------

    private void checkAxisX(Entity entity, EntityBounds bounds) {
        int dx = entity.getDx();
        if (dx == 0) return; // not moving

        //anticipate the entity movement (move left or right)
        int projectedLeftCol = (bounds.leftX + dx) / gameModel.getTILE_SIZE();
        int projectedRightCol = (bounds.rightX + dx) / gameModel.getTILE_SIZE();
        int rowTop = bounds.topY / gameModel.getTILE_SIZE();
        int rowBottom = bounds.bottomY / gameModel.getTILE_SIZE();

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
        int colLeft = bounds.leftX / gameModel.getTILE_SIZE();
        int colRight = bounds.rightX / gameModel.getTILE_SIZE();
        int projectedTopRow = (bounds.topY + dy) / gameModel.getTILE_SIZE();
        int projectedBottomRow = (bounds.bottomY + dy) / gameModel.getTILE_SIZE();


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
    private boolean overlaps(int left, int right, int top, int bottom,
                             int objLeft, int objRight, int objTop, int objBottom) {
        boolean overlapX = right >= objLeft && left <= objRight;
        boolean overlapY = bottom >= objTop && top <= objBottom;
        return overlapX && overlapY;
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
