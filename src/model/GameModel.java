package model;

import main.ENUM.GameState;
import main.ENUM.PlayerState;
import main.CONFIG.GameConfig;
import model.collision.CollisionChecker;
import model.entity.Player;
import model.entity.Monk;
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

    private final GameConfig gameConfig;

    private final CollisionChecker collisionChecker;

    private final GameMap worldGameMap;
    private final ObjectManager objectManager;

    private final Player player;
    private final Monk monk;

    private GameState gameState;
    private boolean debugMode = false;


    /**
     * COSTRUCTOR
      */
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        worldGameMap = new GameMap(GS.mapConfig(), GS.mapDoc());
        player = new Player(GS.entityConfig());

        collisionChecker = new CollisionChecker(this);
        objectManager = new ObjectManager(GS.ObjConfig(), GS.mapDoc());

        monk = new Monk(GS.entityConfig().MONK_START_X(), GS.entityConfig().MONK_START_Y(), GS.entityConfig());

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
    //-------------------------------------------------------------
    //TODO controllare bene

    /**
     * Interactions with objects
     */
    //-------------------------------------------------------------
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

            //TODO interazione con il monaco che va fatta in collison chechker

            // oggetti che richiedono vicinanza con il player
            /*
            double dist = Math.sqrt(Math.pow(player.getWorldX() - monk.getWorldX(), 2) +
                                    Math.pow(player.getWorldY() - monk.getWorldY(), 2));
            if (obj instanceof Monk monk) {
                if (dist < Monk.DETECTION_RADIUS) {  // In range

                    if (monk.getState() == Monk.MonkState.IDLE) {
                        monk.interact();
                        this.currentDialogue = monk.getCurrentDialogue();
                    }

                    if (input.interact()) {
                        monk.advanceDialogue();

                        if (!monk.hasFinishedDialogue()) {
                            this.currentDialogue = monk.getCurrentDialogue();
                        } else {
                            this.currentDialogue = "";
                            monk.setState(Monk.MonkState.DISAPPEARING);
                        }
                    }
                } else {
                    // 3. FUORI RAGGIO: Reset se il giocatore si allontana
                    if (monk.getState() == Monk.MonkState.TALKING) {
                        monk.reset();
                        this.currentDialogue = "";
                    }
                }
            }*/
        }

    }
    //-------------------------------------------------------------

    //TODO ordine
    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public List<GameObject> getObjects() { return objectManager.getObjects(); }
    public ObjectManager getObjectManager() { return objectManager; }
    public boolean isDebugMode() { return debugMode; }
    public int getTILE_SIZE(){ return gameConfig.screenConfig().TILE_SIZE(); }
    public Monk getMonk() {
        return monk;
    }
    //---------------------------------

    // SETTER ----------------------
    public void setGameState(GameState gameState) { this.gameState = gameState; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    //---------------------------------


}
//-------------------------------------------------------------------------------------------------------------------
