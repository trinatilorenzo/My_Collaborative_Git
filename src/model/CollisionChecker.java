package model;

import model.entity.Entity;
import model.object.GameObject;

import java.awt.Rectangle;
import java.util.List;

import model.entity.Player;

import static main.CONFIG.enu.Direction.*;

/**
 * The COLLISIONCHEKER CLASS is responsible for determining whether an entity
 * within the game world collides with the game's tilemap, objects, or other entities.
 */
//-------------------------------------------------------------------------------------------------------------------
public class CollisionChecker {

    private final GameModel gameModel; // dependency from the class

    /**
     * the enty bounds is the way to represent the area of the entity inside the model
     * it is the basics value to check the collision between the entity and the game world
     */
    private record EntityBounds(int leftX, int rightX, int topY, int bottomY, int layer) {

        //methods to create the bounds of the entity
        static EntityBounds of(Entity entity) {
            // worldX & worldY = center of solid area
            int leftX = entity.getWorldX() - entity.getSolidArea().width / 2;
            int rightX = entity.getWorldX() + entity.getSolidArea().width / 2 - 1;
            int topY = entity.getWorldY() - entity.getSolidArea().height / 2;
            int bottomY = entity.getWorldY() + entity.getSolidArea().height / 2 - 1;
            return new EntityBounds(leftX, rightX, topY, bottomY, entity.getCurrentLayer());
        }
    }

    /**
     * COSTRUCTOR
     */
    //-------------------------------------------------------------
    public CollisionChecker(GameModel model) {
        this.gameModel = model;
    }
    //-------------------------------------------------------------


    /**
     * Checks for potential collisions between the given entity and the game world tiles
     */
    //----------------------------------------------------------------------------
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
    //-------------------------------------------------------------
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
            // check if the entity is on the stairs and update the layer
            if (entity instanceof Player){
                //only update the player layer, other entities simply can't move on the stairs
                entity.setCollisionY(true);
                updateEntityLayer(entity, bounds, checkRow, colLeft);
                return;
            }
            entity.setCollisionY(true);
        }
    }
    //-------------------------------------------------------------
    private void updateEntityLayer(Entity entity, EntityBounds bounds, int checkRow, int colLeft){
        int dy = entity.getDy();
        if (dy > 0){
            if (!isCollision(bounds.layer - 1, checkRow, colLeft)) {
            // move level down
            entity.setLayer(entity.getCurrentLayer() - 1);
            entity.setCollisionY(false);
            }
        }else if (dy < 0){
            if (!isCollision(bounds.layer + 1, checkRow, colLeft)) {
                // move level up
                entity.setLayer(entity.getCurrentLayer() + 1);
                entity.setCollisionY(false);
            }
        }
    }
    //-------------------------------------------------------------
    //Checks if a collision occurs at the specified layer, row, and column within the game world.
    //-------------------------------------------------------------
    private boolean isCollision(int layer, int row, int col) {
        if (isOutOfBounds(row, col)) {
            return true;
        }
        return gameModel.getWorldMap().hasCollision(layer, row, col); // just check the collision map
    }
    //-------------------------------------------------------------
    private boolean isOutOfBounds(int row, int col) {
        return row < 0 || col < 0
                || row >= gameModel.getWorldMap().getMaxMapRow()
                || col >= gameModel.getWorldMap().getMaxMapCol();
    }
    //-------------------------------------------------------------
    // end checkTile -------------------------------------------------------------


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
            if (obj.getLayer() != entity.getCurrentLayer()) continue;
            
            Rectangle r = obj.getSolidWorldArea();
            if (r == null) continue;

            int objLeft = r.x;
            int objRight = r.x + r.width - 1;
            int objTop = r.y;
            int objBottom = r.y + r.height - 1;

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


    /**
     * Checks collision between two entities (mover blocked by obstacle).
     * Sets mover's collision flags per axis.
     */
    //-------------------------------------------------------------
    public boolean checkEntity(Entity mover, Entity obstacle) {
        if (mover.getCurrentLayer() != obstacle.getCurrentLayer()) return false;

        EntityBounds m = EntityBounds.of(mover);
        EntityBounds o = EntityBounds.of(obstacle);

        int dx = mover.getDx();
        int dy = mover.getDy();
        boolean collided = false;

        if (dx != 0 && overlaps(m.leftX + dx, m.rightX + dx, m.topY, m.bottomY,
                o.leftX, o.rightX, o.topY, o.bottomY)) {
            mover.setCollisionX(true);
            collided = true;
        }

        if (dy != 0 && overlaps(m.leftX, m.rightX, m.topY + dy, m.bottomY + dy,
                o.leftX, o.rightX, o.topY, o.bottomY)) {
            mover.setCollisionY(true);
            collided = true;
        }

        return collided;
    }
    //-------------------------------------------------------------

    /**
     * UTILITY METHODS Checks if two rectangles overlap
     */
    //-------------------------------------------------------------
    private boolean overlaps(int left, int right, int top, int bottom,
                             int objLeft, int objRight, int objTop, int objBottom) {
        boolean overlapX = right >= objLeft && left <= objRight;
        boolean overlapY = bottom >= objTop && top <= objBottom;
        return overlapX && overlapY;
    }
    //-------------------------------------------------------------


    //GETTERS
    //-------------------------------------------------------------
    public boolean intersects(Entity a, Entity b) {
        if (a.getCurrentLayer() != b.getCurrentLayer()) return false;

        EntityBounds r1 = EntityBounds.of(a);
        EntityBounds r2 = EntityBounds.of(b);

        return overlaps(r1.leftX, r1.rightX, r1.topY, r1.bottomY,
                r2.leftX, r2.rightX, r2.topY, r2.bottomY);
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
