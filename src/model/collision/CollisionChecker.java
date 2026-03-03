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

        // Calcola i bordi della hitbox in coordinate mondo
        int entityLeftWorldX   = entity.worldX + entity.solidArea.x;
        int entityRightWorldX  = entity.worldX + entity.solidArea.x + entity.solidArea.width - 1;
        int entityTopWorldY    = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height - 1;

        // Calcola le tile corrispondenti ai bordi ATTUALI (senza movimento)
        int entityLeftCol  = entityLeftWorldX  / TILE_SIZE;
        int entityRightCol = entityRightWorldX / TILE_SIZE;
        int entityTopRow   = entityTopWorldY   / TILE_SIZE;
        int entityBottomRow= entityBottomWorldY/ TILE_SIZE;

        int row1, row2, col1, col2;

        switch (entity.direction) {
            case "up":
                // Proietta i bordi TOP nella direzione del movimento
                row1 = (entityTopWorldY - entity.speed) / TILE_SIZE;
                col1 = entityLeftCol;
                col2 = entityRightCol;
                if (isCollision(row1, col1) || isCollision(row1, col2)) {
                    entity.collisionOn = true;
                }
                break;

            case "down":
                row1 = (entityBottomWorldY + entity.speed) / TILE_SIZE;
                col1 = entityLeftCol;
                col2 = entityRightCol;
                if (isCollision(row1, col1) || isCollision(row1, col2)) {
                    entity.collisionOn = true;
                }
                break;

            case "left":
                col1 = (entityLeftWorldX - entity.speed) / TILE_SIZE;
                row1 = entityTopRow;
                row2 = entityBottomRow;
                if (isCollision(row1, col1) || isCollision(row2, col1)) {
                    entity.collisionOn = true;
                }
                break;

            case "right":
                col1 = (entityRightWorldX + entity.speed) / TILE_SIZE;
                row1 = entityTopRow;
                row2 = entityBottomRow;
                if (isCollision(row1, col1) || isCollision(row2, col1)) {
                    entity.collisionOn = true;
                }
                break;
        }
    }

    // Metodo helper: controlla bounds + collisione sul boolean[][][]
    private boolean isCollision(int row, int col) {
        if (row < 0 || col < 0 ||
                row >= model.getWorldMap().getMaxMapRow() ||
                col >= model.getWorldMap().getMaxMapCol()) {
            return true; // fuori mappa = collisione
        }
        return model.getWorldMap().hasCollision(row, col);
    }



}
