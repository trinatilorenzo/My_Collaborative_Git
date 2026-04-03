package model;



import main.ENUM.GameState;
import main.ENUM.PlayerState;
import main.CONFIG.GameConfig;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.object.ObjectManager;
import model.world.GameMap;
import model.object.GameObject;

import model.object.OBJ_Tree;

import input.InputState;

import java.awt.Rectangle;
import java.util.List;


/**
 * ALL THE GAME MODEL STAFF HERE
 * world map, entity, combat, AI, events ...
*/
//-------------------------------------------------------------------------------------------------------------------
public class GameModel {

    private GameConfig gameConfig;
    private GameMap worldGameMap;
    private Player player;
    private CollisionChecker collisionChecker;
    private ObjectManager objectManager;
    private OBJ_Monk monk; //FOR TESTING PURPOSES, TO BE REMOVED
    private GameState gameState;
    private boolean debugMode = false;
    private String currentDialogue = "";

    // COSTRUCTOR
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        worldGameMap = new GameMap(GS.mapConfig(), GS.mapDoc());
        player = new Player(GS.playerConfig());

        collisionChecker = new CollisionChecker(this);
        objectManager = new ObjectManager(GS.ObjConfig(), GS.mapDoc());
        monk = new OBJ_Monk(62 * TILE_SIZE, 18* TILE_SIZE); // Posizione di test
        objectManager.add(monk);
        gameState = GameState.PLAYING;
        
    }
    //-------------------------------------------------------------

    /**
     * Update the model status
     */
    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {

        if (gameState == GameState.PLAYING) {
            player.update(input, deltaMs);

            collisionChecker.checkTile(player);
            collisionChecker.checkObjects(player);
            if (player.getState() == PlayerState.WALKING) {
                player.move();
            }
            updateInteractions(input);

            objectManager.update(deltaMs);
        }
    }

    // Interactions with objects
    private void updateInteractions(InputState input) {

        for (GameObject obj : objectManager.getObjects()) {

            if (obj.isRemoved()) continue; // Skip removed objects

            // oggetti che richiedono attacco
            Rectangle attackArea = player.getAttackArea();
            if (player.getState() == PlayerState.ATTACKING) {
                if (obj instanceof OBJ_Tree) {
                    OBJ_Tree tree = (OBJ_Tree) obj;

                    // Se collide con l'area di attacco → colpisci
                    if (attackArea.intersects(tree.getSolidWorldArea())) {
                        tree.interact(); // qui hit() viene chiamato → chopped = true se health <= 0
                    }
                }
            }

            // oggetti che richiedono vicinanza con il player
            double dist = Math.sqrt(Math.pow(player.getWorldX() - monk.getWorldX(), 2) +
                                    Math.pow(player.getWorldY() - monk.getWorldY(), 2));
            if (obj instanceof OBJ_Monk monk) {
                if (dist < OBJ_Monk.DETECTION_RADIUS) {  // In range

                    if (monk.getState() == OBJ_Monk.MonkState.IDLE) {
                        monk.interact();
                        this.currentDialogue = monk.getCurrentDialogue();
                    }

                    if (input.interact()) {
                        monk.advanceDialogue();

                        if (!monk.hasFinishedDialogue()) {
                            this.currentDialogue = monk.getCurrentDialogue();
                        } else {
                            this.currentDialogue = "";
                            monk.setState(OBJ_Monk.MonkState.DISAPPEARING);
                        }
                    }
                } else {
                    // 3. FUORI RAGGIO: Reset se il giocatore si allontana
                    if (monk.getState() == OBJ_Monk.MonkState.TALKING) {
                        monk.reset();
                        this.currentDialogue = "";
                    }
                }
            }
        }

    }
    //-------------------------------------------------------------
    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public List<GameObject> getObjects() { return objectManager.getObjects(); }
    public ObjectManager getObjectManager() { return objectManager; }
    public boolean isDebugMode() { return debugMode; }
    public String getCurrentDialogue() { return currentDialogue; }
    public int getTILE_SIZE(){ return gameConfig.screenConfig().TILE_SIZE(); }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    //---------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
