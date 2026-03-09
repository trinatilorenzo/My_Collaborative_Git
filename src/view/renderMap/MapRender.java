package view.renderMap;

import model.world.GameMap;
import model.entity.Player;

import java.awt.*;

import static main.GameSetting.*;

/**
 * The MAP RENDER CLASS is responsible for rendering the visual representation of the game world.
 */

//-------------------------------------------------------------------------------------------------------------------
public class MapRender {

    /**
     * Draw all the layers of the gameMap
     */
    // -----------------------------------------------------
    public void DrawMap(GameMap gameMap, TileSet tileSet, Player player, Graphics2D g2){
        /*-----------------------------------------------
        DEBUG : draw a single layer
        drawLayer(0, gameMap, tileSet, g2);
        drawLayer(1, gameMap, tileSet, g2);
        drawLayer(2, gameMap, tileSet, g2);
        drawLayer(3, gameMap, tileSet, g2);
        drawLayer(4, gameMap, tileSet, g2);
        -----------------------------------------------*/
        for (int i = 0; i < gameMap.getGraphicLayerNum(); i++) {
            drawLayer(i, gameMap, tileSet, player, g2);
        }
    }
    //-----------------------------------------------------

    /**
     * Draw the single layer of the map
     */
    // -----------------------------------------------------
    void drawLayer(int layer, GameMap gameMap, TileSet tileSet, Player player, Graphics2D g2){

        int leftCol = Math.max(0, (player.getWorldX() - player.getScreenX())/ TILE_SIZE);
        int rightCol = Math.min(gameMap.getMaxMapCol()-1, player.getWorldX() + (SCREEN_WIDTH - player.getScreenX()) / TILE_SIZE );
        int topRow = Math.max(0, (player.getWorldY() - player.getScreenY()) / TILE_SIZE );
        int bottomRow = Math.min(gameMap.getMaxMapRow()-1, (player.getWorldY() + (SCREEN_HEIGHT - player.getScreenY())) / TILE_SIZE );

        for (int i = topRow; i<= bottomRow; i++){
            for (int j = leftCol; j<= rightCol; j++){
                
                int tileID = gameMap.getMapTile(layer, i, j);// get the tile id to know witch tile to render

                if (tileID != -1){//-1 = trasparente
                    drawTile(tileSet.getTileIdToDraw(tileID), i, j, tileSet, player, g2);
                }
            }
        }

    }
    //-----------------------------------------------------


    /**
     * Draw a single tile
     */
    // -----------------------------------------------------
    void drawTile(int tileId, int row, int col, TileSet tileSet, Player player, Graphics2D g2) {

        int worldX = col * TILE_SIZE;
        int worldY = row * TILE_SIZE;

        int screenX = worldX - player.getWorldX() + player.getScreenX();
        int screenY = worldY - player.getWorldY() + player.getScreenY();

        int dx2 = screenX + TILE_SIZE;
        int dy2 = screenY + TILE_SIZE;

        int sx1 = tileSet.getTileX(tileId);
        int sy1 = tileSet.getTileY(tileId);
        int sx2 = sx1 + TILE_SIZE;
        int sy2 = sy1 + TILE_SIZE;


        g2.drawImage(tileSet.getTileSetImg(), 
                     screenX, screenY, dx2, dy2, 
                     sx1, sy1, sx2, sy2, null);
    }
    //-----------------------------------------------------

    //DEBUG MODE
    //-------------------------------------------------------------

    /**
     * Draws a semi-transparent overlay on all collision tiles across all game layers.
     * The player is needed only to compute the camera offset (world → screen conversion).
     */
    public void drawAllGameLayers(GameMap gameMap, Player player, Graphics2D g2) {

        Color[] layerColors = {
                new Color(255, 0, 0, 80),   // rosso  - layer 0
                new Color(0, 0, 255, 80),   // blu    - layer 1
                new Color(0, 255, 0, 80),   // verde  - layer 2
        };

        // camera offset
        int camOffsetX = -player.getWorldX() + player.getScreenX();
        int camOffsetY = -player.getWorldY() + player.getScreenY();

        Stroke originalStroke = g2.getStroke();
        Font originalFont = g2.getFont();
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("Arial", Font.BOLD, 10));

        for (int layer = GAME_LAYER_NUM -1; layer >= 0 ; layer--) {
            Color fill = layer < layerColors.length
                    ? layerColors[layer]
                    : new Color(255, 255, 0, 80);
            Color border = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 160);

            for (int row = 0; row < gameMap.getMaxMapRow(); row++) {
                for (int col = 0; col < gameMap.getMaxMapCol(); col++) {

                    if (gameMap.hasCollision(layer, row, col) ) continue;

                    int screenX = col * TILE_SIZE + camOffsetX;
                    int screenY = row * TILE_SIZE + camOffsetY;


                    if (screenX + TILE_SIZE < 0 || screenX > SCREEN_WIDTH ||
                            screenY + TILE_SIZE < 0 || screenY > SCREEN_HEIGHT) continue;

                    g2.setColor(fill);
                    g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);

                    g2.setColor(border);
                    g2.drawRect(screenX, screenY, TILE_SIZE, TILE_SIZE);

                    g2.setColor(Color.WHITE);
                    g2.drawString("L" + layer, screenX + 2, screenY + 12);
                }
            }
        }

        g2.setStroke(originalStroke);
        g2.setFont(originalFont);
    }

    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
