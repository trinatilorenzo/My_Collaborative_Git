package model.collision;
import model.entity.Entity;
import model.GameModel;
import static main.GameSetting.*;

public class CollisionChecker {

    private GameModel model;

    public CollisionChecker(GameModel model) {
        this.model = model;
    }

    public void checkTile(Entity entity) {
        entity.collisionOn = false;

        // Calculate the entity's solid area position in the world
        int entityLeftWorldX   = entity.getWorldX() + entity.getSolidArea().x;
        int entityRightWorldX  = entity.getWorldX() + entity.getSolidArea().x + entity.getSolidArea().width - 1;
        int entityTopWorldY    = entity.getWorldY() + entity.getSolidArea().y;
        int entityBottomWorldY = entity.getWorldY() + entity.getSolidArea().y + entity.getSolidArea().height - 1;

        // Calcola le tile corrispondenti ai bordi ATTUALI (senza movimento)
        int entityLeftCol  = entityLeftWorldX  / TILE_SIZE;
        int entityRightCol = entityRightWorldX / TILE_SIZE;
        int entityTopRow   = entityTopWorldY   / TILE_SIZE;
        int entityBottomRow= entityBottomWorldY/ TILE_SIZE;

        int row1, row2, col1, col2;
        int layer = entity.getCurrentLayer();
        switch (entity.getDirection()) {
            
            case UP:
                // Proietta i bordi TOP nella direzione del movimento
                row1 = (entityTopWorldY - entity.getSpeed()) / TILE_SIZE;
                col1 = entityLeftCol;
                col2 = entityRightCol;
                if (isCollision(layer, row1, col1) || isCollision(layer, row1, col2)) {
                    entity.collisionOn = true;
                }
                break;

            case DOWN:
                row1 = (entityBottomWorldY + entity.getSpeed()) / TILE_SIZE;
                col1 = entityLeftCol;
                col2 = entityRightCol;
                if (isCollision(layer, row1, col1) || isCollision(layer, row1, col2)) {
                    entity.collisionOn = true;
                }
                break;

            case LEFT:
                col1 = (entityLeftWorldX - entity.getSpeed()) / TILE_SIZE;
                row1 = entityTopRow;
                row2 = entityBottomRow;
                if (isCollision(layer, row1, col1) || isCollision(layer, row2, col1)) {
                    entity.collisionOn = true;
                }
                break;

            case RIGHT:
                col1 = (entityRightWorldX + entity.getSpeed()) / TILE_SIZE;
                row1 = entityTopRow;
                row2 = entityBottomRow;
                if (isCollision(layer, row1, col1) || isCollision(layer, row2, col1)) {
                    entity.collisionOn = true;
                }
                break;
        }
    }

    // Metodo helper: controlla bounds + collisione sul boolean[][][]
    private boolean isCollision(int layer, int row, int col) {
        if (row < 0 || col < 0 ||
                row >= model.getWorldMap().getMaxMapRow() ||
                col >= model.getWorldMap().getMaxMapCol()) {
            return true; // out of bounds = collision
        }
        return model.getWorldMap().hasCollision(layer, row, col);
    }



}
