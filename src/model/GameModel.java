package model;

import controller.InputState;
import main.CONFIG.EntityConfig;
import main.CONFIG.GameConfig;
import main.CONFIG.ObjConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.UIConfig;
import main.CONFIG.enu.*;
import model.entity.*;
import model.event.AudioEventType;
import model.object.*;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ALL THE GAME MODEL STAFF HERE
 * world map, entity, combat, AI, events ...
*/
//-------------------------------------------------------------------------------------------------------------------
public class GameModel implements Serializable {
    private transient GameConfig gameConfig;

    @Serial
    private static final long serialVersionUID = 1L;
    //-------------------------------------------------------------

    // Game status
    private GameState gameState;
    private boolean debugMode;
    //-------------------------------------------------------------

    // Collision
    private transient CollisionChecker collisionChecker;
    //-------------------------------------------------------------

    // Map & OBJ
    private  GameMap worldGameMap;
    private List<GameObject> objects;
    //-------------------------------------------------------------

    // Player & NPC
    //-------------------------------------------------------------
    private Player player;
    private Monk monk;
    private List<EnemyTNT> tntEnemies;
    private List<EnemyDynamite> dynamiteEnemies;
    private List<DynamiteProjectile> projectiles;
    private List<EnemyTorch> torchEnemies;
    //-------------------------------------------------------------

    //UI State

    private boolean settingsMenuOpen, settingsPauseOpen;
    private boolean musicEnabled, soundEnabled;
    private int resValue; // 0 = Max , 1 = Mid, 2 = Low
    private PlayerColor playerColor;


    // Dialogue
    private String currentDialogue; // dialogue currently displayed to the player
    private String currentMessage;
    private double messageTimer;
    //-------------------------------------------------------------

    // Death sequence
    private double deadStateElapsedMs;

    // Win sequence
    private double winStateElapsedMs;

    //Audio events
    private transient List<AudioEventType> pendingAudioEvents = new ArrayList<>();

    // Level and progression
    private int currentLevel = 1;
    private boolean levelCompleted;
    private boolean currentLevelPowerUpCollected = false;


    //-------------------------------------------------------------
    /**
     * CONSTRUCTOR
      */
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        worldGameMap = new GameMap(GS.mapConfig(), GS.mapDoc());
        collisionChecker = new CollisionChecker(this);

        gameState = GameState.MENU; // Default game state is the menu state
        debugMode = false; // Default debug mode is off

        currentDialogue = "";
        currentMessage = "";
        messageTimer = 0.0;
        deadStateElapsedMs = 0.0;
        winStateElapsedMs = 0.0;

        settingsMenuOpen = false;
        settingsPauseOpen = false;
        playerColor = GS.entityConfig().DEFAULT_COLOR;
        musicEnabled = true;
        soundEnabled = true;
        resValue = 0;

    }
    //-------------------------------------------------------------

    /**
     * Start a new game from scratch
     */
    //-------------------------------------------------------------
    public void initializeNewGame(){
        player = new Player(gameConfig.entityConfig(), playerColor);

        //initialize NPC
        EntityConfig entityConfig = gameConfig.entityConfig();
        monk = new Monk(entityConfig.MONK_START_X(), entityConfig.MONK_START_Y(), entityConfig);
        tntEnemies = spawnTntEnemies(entityConfig);
        projectiles = new ArrayList<>();
        dynamiteEnemies = spawnDynamiteEnemies(entityConfig, projectiles);
        torchEnemies = spawnTorchEnemies(entityConfig);
        
        //initialize Objects
        ObjConfig objC = gameConfig.ObjConfig();
        objects = new ArrayList<>();

        // first level tree
        spawnTrees(objC.TREES_03_SPAWNPOINT(), objC.TREE_TAG_03(), objC.TREE_03_WIDTH, objC.TREE_03_HEIGHT,
                new Dimension(objC.TREE_03_HITBOX_WIDTH, objC.TREE_03_HITBOX_HEIGHT), objC.TREE_03_HITBOX_OFFSET_Y, objC);
        // second level tree
        spawnTrees(objC.TREES_02_SPAWNPOINT(), objC.TREE_TAG_02(), objC.TREE_02_WIDTH, objC.TREE_02_HEIGHT,
                new Dimension(objC.TREE_02_HITBOX_WIDTH, objC.TREE_02_HITBOX_HEIGHT) ,objC.TREE_02_HITBOX_OFFSET_Y, objC);
        // third level tree
        spawnTrees(objC.TREES_01_SPAWNPOINT(), objC.TREE_TAG_01(), objC.TREE_01_WIDTH, objC.TREE_01_HEIGHT,
                new Dimension(objC.TREE_01_HITBOX_WIDTH, objC.TREE_01_HITBOX_HEIGHT), objC.TREE_01_HITBOX_OFFSET_Y, objC);

        // buildings
        spawnBuildings(objC.CASTLE_SPAWNPOINT(), objC.CASTLE_TAG(), ObjConfig.CASTLE_WIDTH, ObjConfig.CASTLE_HEIGHT,
                ObjConfig.CASTLE_HITBOX_WIDTH, ObjConfig.CASTLE_HITBOX_HEIGHT, ObjConfig.CASTLE_HITBOX_OFFSET_Y, objC);
        spawnBuildings(objC.TOWER_SPAWNPOINT(), objC.TOWER_TAG(), ObjConfig.TOWER_WIDTH, ObjConfig.TOWER_HEIGHT,
                ObjConfig.TOWER_HITBOX_WIDTH, ObjConfig.TOWER_HITBOX_HEIGHT, ObjConfig.TOWER_HITBOX_OFFSET_Y, objC);
        spawnBuildings(objC.GOLDMINE_SPAWNPOINT(), objC.GOLDMINE_TAG(), ObjConfig.GOLDMINE_WIDTH, ObjConfig.GOLDMINE_HEIGHT,
                ObjConfig.GOLDMINE_HITBOX_WIDTH, ObjConfig.GOLDMINE_HITBOX_HEIGHT, ObjConfig.GOLDMINE_HITBOX_OFFSET_Y, objC);
        spawnBuildings(objC.GOBLIN_HOME_SPAWNPOINT(), objC.GOBLIN_HOME_TAG(), ObjConfig.GOBLIN_HOME_WIDTH, ObjConfig.GOBLIN_HOME_HEIGHT,
                ObjConfig.GOBLIN_HOME_HITBOX_WIDTH, ObjConfig.GOBLIN_HOME_HITBOX_HEIGHT, ObjConfig.GOBLIN_HOME_HITBOX_OFFSET_Y, objC);

        // Power ups settings
        assignRandomPowerUps(objC);
        currentLevelPowerUpCollected = false;
        currentLevel = 1; 

        // START THE GAME
        gameState = GameState.PLAYING;
    }
    /**
     * HELPERS METHOD
     */
    private List<EnemyTNT> spawnTntEnemies(EntityConfig entityConfig) {
        List<EnemyTNT> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.TNT_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.TNT_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyTNT(spawnPoint, entityConfig));
            }
        }
        return enemies;
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn dynamite enemies based on the spawn points defined.
      * It creates an EnemyDynamite instance for each spawn point and adds it to the list of enemies.
     */
    private List<EnemyDynamite> spawnDynamiteEnemies(EntityConfig entityConfig, List<DynamiteProjectile> projectileStore) {
        List<EnemyDynamite> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.DYNAMITE_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.DYNAMITE_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyDynamite(spawnPoint, entityConfig, projectileStore));
            }
        }
        return enemies;
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn torch enemies based on the spawn points.
     * It creates an EnemyTorch instance for each spawn point and adds it to the list of enemies.
    */
    private List<EnemyTorch> spawnTorchEnemies(EntityConfig entityConfig) {
        List<EnemyTorch> enemies = new ArrayList<>();
        for (SpawnPoint spawnPoint : entityConfig.TORCH_SPAWNPOINT()) {
            for (int i = 0; i < entityConfig.TORCH_FOR_SPAWNPOINT; i++) {
                enemies.add(new EnemyTorch(spawnPoint, entityConfig));
            }
        }
        return enemies;
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn trees based on the spawn points defined in the ObjConfig.
      * It creates an OBJ_Tree instance for each spawn point and adds it to the list of game objects.
     */
    private void spawnTrees(List<SpawnPoint> spawnPoints, String treeTag, int treeWidth, int treeHeight, Dimension hitboxDim, int hitboxOffsetY, ObjConfig objConfig) {
        for (SpawnPoint spawnPoint : spawnPoints) {
            objects.add(new OBJ_Tree(objConfig, treeTag, spawnPoint, treeWidth, treeHeight,
                    createTreeSolidArea(treeWidth, hitboxDim, hitboxOffsetY, objConfig)
            ));
        }
    }
    //-------------------------------------------------------------
    /**
     * Helper method to spawn buildings based on the spawn points defined in the ObjConfig.
     */
    private void spawnBuildings(List<SpawnPoint> spawnPoints, String buildingTag, int buildingWidth, int buildingHeight,
                                int hitboxWidth, int hitboxHeight, int hitboxOffsetY, ObjConfig objConfig) {
        for (SpawnPoint spawnPoint : spawnPoints) {
            GameObject building = new GameObject(
                    objConfig,
                    buildingTag,
                    spawnPoint,
                    buildingWidth,
                    buildingHeight,
                    createBuildingSolidArea(buildingWidth, hitboxWidth, hitboxHeight, hitboxOffsetY),
                    ObjConfig.BUILDING_SOLID
            );

            // Makes the gold mine not solid
            if (buildingTag.equals(objConfig.GOLDMINE_TAG())) {
                building.setSolid(false); 
            }

            objects.add(building);
        }
    }
    //-------------------------------------------------------------
    /**
     * Helper method to create a solid area for trees. 
      * It calculates the position and size of the solid area relative to the tree's visual representation.
     */
    private Rectangle createTreeSolidArea(int treeWidth, Dimension treeHitbox, int hitboxOffsetY, ObjConfig objConfig) {
        return new Rectangle(
                treeWidth / 2 - ( treeHitbox.width/ 2),
                hitboxOffsetY, treeHitbox.width, treeHitbox.height);
    }
    //-------------------------------------------------------------
    /**
     * Helper method to create a solid area for buildings.
     */
    private Rectangle createBuildingSolidArea(int buildingWidth, int hitboxWidth, int hitboxHeight, int hitboxOffsetY) {
        return new Rectangle(
                buildingWidth / 2 - (hitboxWidth / 2),
                hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
        );
    }
    /**
     * Assigns random power-ups to a subset of trees in the game world.
     */
    private void assignRandomPowerUps(ObjConfig objC){
        assignPowerUpToRandomTree(objC.TREE_TAG_03(), PowerUpType.SHIELD); 
        assignPowerUpToRandomTree(objC.TREE_TAG_02(), PowerUpType.HEALTH_RESTORE); 
        assignPowerUpToRandomTree(objC.TREE_TAG_01(), PowerUpType.SPEED_BOOST); 

    }
    //---------------------------------------------
    private void assignPowerUpToRandomTree(String treeTag, PowerUpType type){
        List<OBJ_Tree> validTrees = new ArrayList<>();
        for (GameObject obj: objects) {
            if (obj instanceof OBJ_Tree tree && tree.getName().equals(treeTag)){
                validTrees.add(tree);
            }
        }
        if (!validTrees.isEmpty()) {
            int randomIndex = (int) (Math.random() * validTrees.size());
            validTrees.get(randomIndex).setHiddenPowerUp(type);
            System.out.println("Assigned " + type + " to tree: " + validTrees.get(randomIndex).getName() + " at (" + validTrees.get(randomIndex).getWorldX() + ", " + validTrees.get(randomIndex).getWorldY() + ")");
        }
    }
    //end helpers -------------------------------------------------



    /**
     * MAIN METHOD OF THE CLASS
     * Update the model status, Called by the controller every frame
     */
    //-------------------------------------------------------------
    public void update(InputState input, double deltaMs) {

        switch (gameState) {
            case MENU:
                // no update for menu state
                break;
            case PLAYING:
                updatePlayingState(input, deltaMs);
                break;
            case PAUSED:
                updateState(input);
                break;
            case SETTINGS:
                // no update for settings state
                break;
            case GAME_OVER:
                // no update for game over state
                break;
            case WIN:
                // no update for game win state
                break;
        }

    }
    //-------------------------------------------------------------

    // ALL THE UPDATE METHOD
    //-------------------------------------------------------------
    //-------------------------------------------------------------
    private void updatePlayingState(InputState input, double deltaMs) {

        int lifeBeforeUpdate = player.getLife();
        updatePlayer(input, deltaMs);
        updateEnemies(deltaMs);
        updateMonk(input);

        if (player.getState() == PlayerState.WALKING) {
            player.move();
        }

        List<GameObject> toSpawn = new ArrayList<>();
        for (GameObject obj : objects) {
            if (!obj.isRemoved()) {
                obj.update(deltaMs);

                // if the tree has been chopped and has a hidden power-up, spawn the power-up object
                if (obj instanceof OBJ_Tree tree && tree.shouldDropPowerUp()){
                    Rectangle treeHitbox = tree.getSolidArea();
                    int powerUpX = tree.getWorldX() + treeHitbox.x + (treeHitbox.width - gameConfig.ObjConfig().POWER_UP_SIZE) / 2;
                    int powerUpY = tree.getWorldY() + treeHitbox.y + (treeHitbox.height - gameConfig.ObjConfig().POWER_UP_SIZE) / 2;
                    toSpawn.add(new OBJ_PowerUp(gameConfig.ObjConfig(), tree.getHiddenPowerUp(), powerUpX, powerUpY, tree.getLayer()));
                }
            }
        }
        if (!toSpawn.isEmpty()) {
            objects.addAll(toSpawn);
        }
        objects.removeIf(GameObject::isRemoved);

        monk.update(player, deltaMs);
        updateInteractions();
        updateMessage(deltaMs);

        updateEvents(lifeBeforeUpdate);
        updateState(input);

    }
    //-------------------------------------------------------------
    private void updatePlayer(InputState input, double deltaMs) {
        PlayerState playerStateBeforeUpdate = player.getState();

        // DEATH
        if (player.isDying() || player.isDead()) {
            updateGameOverCountdown(deltaMs);
            updateDeathSequence(deltaMs);
            return;
        }

        player.update(input, deltaMs);

        collisionChecker.checkTile(player);
        collisionChecker.checkObjects(player);

        //Audio ----------------------
        if (playerStateBeforeUpdate != PlayerState.ATTACKING && player.getState() == PlayerState.ATTACKING) {
            emitAudioEvent(AudioEventType.PLAYER_ATTACK);
        }
        if (playerStateBeforeUpdate == PlayerState.ATTACKING && player.getState() != PlayerState.ATTACKING) {
            emitAudioEvent(AudioEventType.PLAYER_ATTACK_STOP);
        }
        if (playerStateBeforeUpdate != PlayerState.WALKING && player.getState() == PlayerState.WALKING) {
            emitAudioEvent(AudioEventType.PLAYER_WALK_START);
            System.out.println("Player started walking");
        }
        if (playerStateBeforeUpdate == PlayerState.WALKING && player.getState() != PlayerState.WALKING) {
            System.out.println("Player stopped walking");
            emitAudioEvent(AudioEventType.PLAYER_WALK_STOP);
        }
        //----------------------------
    }
    //-------------------------------------------------------------
    private void updateEnemies(double deltaMs) {
        // Update TNT
        for (EnemyTNT tnt : tntEnemies) {
            TNTState previousState = tnt.getState();
            if (tnt.getState() != TNTState.EXPLODED) {

                tnt.update(player, deltaMs);
                collisionChecker.checkEntity(player, tnt);
                collisionChecker.checkTile(tnt);
                collisionChecker.checkObjects(tnt);
                tnt.move();
            }

            //Audio ----------------------
            if (previousState != tnt.getState() && tnt.getState() == TNTState.EXPLODING) {
                emitAudioEvent(AudioEventType.TNT_EXPLOSION);
            }
            if (previousState != tnt.getState() && tnt.getState() == TNTState.TRIGGERED) {
                emitAudioEvent(AudioEventType.TNT_TRIGGERED);
            }
            //----------------------------
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        // Update Dynamite
        for (EnemyDynamite dynamite : dynamiteEnemies) {
            DynamiteState previousState = dynamite.getState();
            if (dynamite.getState() != DynamiteState.DEAD){

                dynamite.update(player, deltaMs);
                collisionChecker.checkEntity(player, dynamite);
                collisionChecker.checkTile(dynamite);
                collisionChecker.checkObjects(dynamite);
                dynamite.move();

                //Audio ----------------------
                if (previousState != DynamiteState.ATTACKING && dynamite.getState() == DynamiteState.ATTACKING) {
                    emitAudioEvent(AudioEventType.PROJECTILE_LAUNCHED);
                }
                //----------------------------
            }

        }
        dynamiteEnemies.removeIf(EnemyDynamite::isDead);

        // Dynamite projectiles
        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);

            if (collisionChecker.intersects(player, proj)) {
                player.takeDamage();
                proj.explode();
            }

            //Audio ----------------------
            if (proj.isExploded()) {
                emitAudioEvent(AudioEventType.PROJECTILE_EXPLODED);
            }
            //----------------------------
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);

        // Update Torch
        for (EnemyTorch torch : torchEnemies) {
            TorchState previousState = torch.getState();
            if (torch.getState() != TorchState.DEAD){

                torch.update(player, deltaMs);
                collisionChecker.checkEntity(torch, player);
                collisionChecker.checkEntity(player, torch);
                collisionChecker.checkTile(torch);
                collisionChecker.checkObjects(torch);
                torch.move();

                //Audio ----------------------
              
                //----------------------------
            }

        }
        torchEnemies.removeIf(EnemyTorch::isDead);
    }
    //-------------------------------------------------------------
    private void updateInteractions() {

        //Player attack with sword
        if (player.getState() == PlayerState.ATTACKING) {
            Rectangle attackArea = player.getAttackArea();

            // TNT -------------------
            for (EnemyTNT tnt : tntEnemies) {
                if (!player.isAttackDamageApplied() && attackArea.intersects(tnt.getSolidWorldArea())) {
                    tnt.takeDamage();
                    player.setAttackDamageApplied(true);
                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.ENEMY_HIT);
                }
            }
            // -------------------

            // Dynamite -------------------
            for (EnemyDynamite dynamite : dynamiteEnemies) {
                if (!player.isAttackDamageApplied() && attackArea.intersects(dynamite.getSolidWorldArea())) {
                    dynamite.takeDamage();
                    player.setAttackDamageApplied(true);
                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.ENEMY_HIT);
                }
            }
            // -------------------

            // Torch -------------------
            for (EnemyTorch torch : torchEnemies) {
                if (!player.isAttackDamageApplied() && attackArea.intersects(torch.getSolidWorldArea())) {
                    torch.takeDamage();
                    player.setAttackDamageApplied(true);
                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.ENEMY_HIT);
                }
            }
            // -------------------

            //Tree -----------
            for (GameObject obj : objects) {
                if (obj.isRemoved()) continue;
                if (obj instanceof OBJ_Tree tree
                        && !player.isAttackDamageApplied()
                        && attackArea.intersects(tree.getSolidWorldArea())
                        && tree.isSolid()) {

                    player.setAttackDamageApplied(true);
                    tree.interact();

                    //Audio ----------------------
                    emitAudioEvent(AudioEventType.TREE_HIT);
                    if (tree.isLastHit()) {
                        System.out.println("Tree chopped!");
                        emitAudioEvent(AudioEventType.TREE_FINAL);
                    }

                }
            }
        }
        for (GameObject obj: objects){
            if (obj.getName().equals(gameConfig.ObjConfig().GOLDMINE_TAG())){
                // Check if the player's hitbox intersects the mine's hitbox
                if (player.getSolidWorldArea().intersects(obj.getSolidWorldArea())) {
                    // Handle win state 
                    if (currentLevel == 3 && levelCompleted) {
                        gameState = GameState.WIN;
                        System.out.println("YOU WIN! Il giocatore ha raggiunto la miniera.");
                        break; 
                    }
                }
            }

            if (obj instanceof OBJ_PowerUp powerUp && player.getSolidWorldArea().intersects(powerUp.getSolidWorldArea())) {
                if (!powerUp.isCollectible()) {
                    continue; // Skip if the power-up is not yet collectible
                }
                player.applyPowerUpEffect(powerUp.getType());
                powerUp.remove(); // Remove the power-up from the game world
                if (isPowerUpForCurrentLevel(powerUp.getType())) {
                    currentLevelPowerUpCollected = true; // Mark the power-up as collected for level progression
                }
                //Audio ----------------------
                //TODO: emitAudioEvent(AudioEventType.POWERUP_COLLECTED);
            }
        }
        // -------------------
        checkLevelProgression();
    }

    private void updateMessage(double deltaMS){

        if (currentMessage.isEmpty()) return;

        messageTimer += deltaMS;
        if (messageTimer >= UIConfig.MESSAGE_TIMER_MS){
            currentMessage="";
            messageTimer = 0;
        }

    }
    //----------------------------------------------------------------------
    /**
     * Update level progression
     */
    private void checkLevelProgression() {
       boolean enemiesDefeated = allEnemiesDefeated();
        
        updateFlashingEffect(enemiesDefeated);

        if (enemiesDefeated){
            if (currentLevelPowerUpCollected) {
                if (currentLevel == 1){
                    currentMessage = "Premi (R) per attivare lo scudo";
                }
                if (currentLevel < 3){
                    currentLevel ++;
                    currentLevelPowerUpCollected = false;
                    System.out.println("Level passed! Current Level: "+currentLevel);
                } else {
                    // all levels passed 
                    updateMonkPositionForEndLevel();
                    levelCompleted = true;
                System.out.println("Level passed! End level "+currentLevel);

                }
            }
        } else {
            levelCompleted = false;

        }
    }
    //-----------------------------------------------------------------------------------
    private void updateFlashingEffect(boolean enemiesDefeated){
        PowerUpType targetType = switch (currentLevel) {
            case 1 -> PowerUpType.SHIELD;
            case 2 -> PowerUpType.HEALTH_RESTORE;
            case 3 -> PowerUpType.SPEED_BOOST;
            default -> null;
        };

        for (GameObject obj : objects) {
            if (obj instanceof OBJ_Tree tree) {
                // If the enemies are defeated and the power-up has not been taken it flashes.
                if (enemiesDefeated && tree.getHiddenPowerUp() == targetType && !currentLevelPowerUpCollected) 
                    tree.setFlashingActive(true); 
                else {
                    tree.setFlashingActive(false);
                }
            }
        }
    }

    //-------------------------------------------------------
    private void updateMonk(InputState input) {
        // Monk Talking ----------------------

        if (monk.getState() == MonkState.IDLE) {
            currentDialogue = "";
        }

        if (monk.getState() == MonkState.TALKING && currentDialogue.isEmpty()) {
            currentDialogue = monk.getCurrentDialogue();
            // Audio ----------
            emitAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
        }

        if (monk.getState() == MonkState.TALKING && input.interact()) {
            monk.nextDialogue();

            if (!monk.hasFinishedDialogue()) {
                currentDialogue = monk.getCurrentDialogue();
                // Audio ----------
                emitAudioEvent(AudioEventType.DIALOGUE_ADVANCE);
            } else {
                currentDialogue = "";
                monk.setState(MonkState.DISAPPEARING);
                // Audio ----------
                emitAudioEvent(AudioEventType.DIALOGUE_CLOSE);
            }
        }
        // ----------------------------------
    }
    //-------------------------------------------------------------
    private void updateMonkPositionForEndLevel(){
        //TODO 
    }
    //-------------------------------------------------------------
    private void updateEvents(int lifeBeforeUpdate) {
        if (player.getLife() < lifeBeforeUpdate) {
            emitAudioEvent(AudioEventType.PLAYER_DAMAGED);
        }
    }
    //-------------------------------------------------------------
    /**
     * Called when player is dying or dead to update only necessary logic and animations
      */
    private void updateDeathSequence(double deltaMs) {
        monk.update(player, deltaMs);
        // Keep only finite transitions running; do not start new gameplay logic.
        for (EnemyTNT tnt : tntEnemies) {
            if (tnt.getState() == TNTState.TRIGGERED || tnt.getState() == TNTState.EXPLODING) {
                tnt.update(player, deltaMs);
            }
        }
        tntEnemies.removeIf(EnemyTNT::isExploded);

        for (DynamiteProjectile proj : projectiles) {
            proj.update(deltaMs);
            collisionChecker.checkTile(proj);
        }
        projectiles.removeIf(DynamiteProjectile::isExploded);

    }
    //-------------------------------------------------------------
    /**
     * Updates the game over countdown timer.
     */
    private void updateGameOverCountdown(double deltaMs) {
        boolean readyForGameOver = player.isDeathAnimationCompleted() && !hasPendingTransientAnimations();
        if (readyForGameOver) {
            deadStateElapsedMs += deltaMs;
        } else {
            deadStateElapsedMs = 0.0;
        }
        if (deadStateElapsedMs >= UIConfig.GAME_OVER_DELAY_MS) {
            System.out.println("GAME OVER");
            gameState = GameState.WIN;
            deadStateElapsedMs = 0.0;
        }
    }
    //-------------------------------------------------------------
    private void updateState(InputState input) {
        if (gameState == GameState.GAME_OVER || gameState == GameState.WIN) return;
        if (player.isDying() || player.isDead()) {
            gameState = GameState.PLAYING;
            return;
        }
        if(input.pause()){
            gameState = GameState.PAUSED;
        }else {
            gameState = GameState.PLAYING;
        }
    }
    //-------------------------------------------------------------
    //end updates method ------------------------------------------


    /**
     * Returns true while finite transition animations are still running.
     */
    //-------------------------------------------------------------
    private boolean hasPendingTransientAnimations() {

        for (EnemyTNT tnt : tntEnemies) {
            if (tnt.getState() == TNTState.TRIGGERED || tnt.getState() == TNTState.EXPLODING) {
                return true;
            }
        }

        if (!projectiles.isEmpty()) {
            return true;
        }

        return false;
    }
    //-------------------------------------------------------------
    
    //-------------------------------------------------------------
    private boolean isPowerUpForCurrentLevel(PowerUpType type) {
        return (currentLevel == 1 && type == PowerUpType.SHIELD) ||
               (currentLevel == 2 && type == PowerUpType.HEALTH_RESTORE) ||
               (currentLevel == 3 && type == PowerUpType.SPEED_BOOST);
    }
    //-------------------------------------------------------------
    
    /**
     * Check if all enemies of current level are defeated
     */
    private boolean allEnemiesDefeated () {
        boolean allEnemiesDefeated = false;
        switch (currentLevel) {
            case 1 -> allEnemiesDefeated = tntEnemies.isEmpty();
            case 2 -> allEnemiesDefeated = dynamiteEnemies.isEmpty();
            case 3 -> allEnemiesDefeated = torchEnemies.isEmpty();
        }
        return allEnemiesDefeated;
    }
    
    /**
     * Audio event emitter
      */
    //-------------------------------------------------------------
    private void emitAudioEvent(AudioEventType audioEventType) {
        pendingAudioEvents.add(audioEventType);
    }
    public List<AudioEventType> consumeAudioEvents() {
        if (pendingAudioEvents.isEmpty()) {
            return List.of();
        }
        List<AudioEventType> snapshot = List.copyOf(pendingAudioEvents);
        pendingAudioEvents.clear();
        return snapshot;
    }
    //-------------------------------------------------------------


    //save and load

    public void beforeSave() {
        if (pendingAudioEvents != null) {
            pendingAudioEvents.clear();
        }
    }

    public void restoreTransientState(GameConfig config) {
        this.gameConfig = config;
        this.collisionChecker = new CollisionChecker(this);

        if (this.pendingAudioEvents == null) {
            this.pendingAudioEvents = new ArrayList<>();
        } else {
            this.pendingAudioEvents.clear();
        }

        ObjConfig objC = config.ObjConfig();
        EntityConfig entC = config.entityConfig();


        if (player != null) player.setEntityConfig(entC);
        if (monk != null) monk.setEntityConfig(entC);

        if (tntEnemies != null) {
            for (EnemyTNT e : tntEnemies) {
                e.setEntityConfig(entC);
            }
        }

        if (dynamiteEnemies != null) {
            for (EnemyDynamite e : dynamiteEnemies) {
                e.setEntityConfig(entC);
            }
        }

        if (torchEnemies != null) {
            for (EnemyTorch e : torchEnemies) {
                e.setEntityConfig(entC);
            }
        }

        if (objects != null) {
            for (GameObject obj : objects) {
                obj.setObjConfig(objC);
            }
        }

        if(worldGameMap != null){
            this.worldGameMap.mConf(config.mapConfig());
        }
    }

    public void afterLoad() {
        this.collisionChecker = new CollisionChecker(this);

        if (this.pendingAudioEvents == null) {
            this.pendingAudioEvents = new ArrayList<>();
        } else {
            this.pendingAudioEvents.clear();
        }

        if (this.objects == null) this.objects = new ArrayList<>();
        if (this.tntEnemies == null) this.tntEnemies = new ArrayList<>();
        if (this.dynamiteEnemies == null) this.dynamiteEnemies = new ArrayList<>();
        if (this.projectiles == null) this.projectiles = new ArrayList<>();
        if (this.torchEnemies == null) this.torchEnemies = new ArrayList<>();
        if (this.currentDialogue == null) this.currentDialogue = "";
        if (this.currentMessage == null) this.currentMessage = "";
    }

    public void copyFrom(GameModel other) {
        this.gameState = other.gameState;
        this.debugMode = other.debugMode;

        this.objects = other.objects;
        this.player = other.player;
        this.monk = other.monk;
        this.tntEnemies = other.tntEnemies;
        this.dynamiteEnemies = other.dynamiteEnemies;
        this.projectiles = other.projectiles;
        this.torchEnemies = other.torchEnemies;

        this.settingsMenuOpen = other.settingsMenuOpen;
        this.settingsPauseOpen = other.settingsPauseOpen;
        this.musicEnabled = other.musicEnabled;
        this.soundEnabled = other.soundEnabled;
        this.resValue = other.resValue;
        this.playerColor = other.playerColor;
        this.currentDialogue = other.currentDialogue;
        this.currentMessage = other.currentMessage;

        this.deadStateElapsedMs = other.deadStateElapsedMs;
        this.currentLevel = other.currentLevel;
        this.levelCompleted = other.levelCompleted;
        this.currentLevelPowerUpCollected = other.currentLevelPowerUpCollected;
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        afterLoad();
    }

    public void forcePlayingState() {
        this.gameState = GameState.PLAYING;
        this.settingsPauseOpen = false;
        this.settingsMenuOpen = false;
    }

    // GETTER ----------------------
    public Player getPlayer() { return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public List<GameObject> getObjects() { return objects; }
    public boolean isDebugMode() { return debugMode; }
    public int getTILE_SIZE(){ return gameConfig.screenConfig().TILE_SIZE(); }
    public Monk getMonk() { return monk; }
    public List<EnemyTNT> getTntEnemies() { return tntEnemies; }
    public List<EnemyDynamite> getDynamiteEnemies() { return dynamiteEnemies; }
    public String getCurrentDialogue() { return currentDialogue; }
    public String getCurrentMessage() { return currentMessage; }
    public List<DynamiteProjectile> getProjectiles(){ return projectiles; }
    public List<EnemyTorch> getTorchEnemies() { return torchEnemies; }
    public GameConfig getGameConfig() { return gameConfig; }
    //---------------------------------

    // SETTER ----------------------
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    public void resumeFromPause() {
        if (gameState == GameState.PAUSED){
            gameState = GameState.PLAYING;
        }
    }
    public void toggleSetingsFormPause(){
        if (settingsPauseOpen){
            settingsPauseOpen = false;
        }else {
            settingsPauseOpen = true;
            settingsMenuOpen = false;
            gameState = GameState.SETTINGS;
        }
    }
    public void toggleSetingsFormMenu(){
        if (settingsMenuOpen){
            settingsMenuOpen = false;
        }else {
            settingsMenuOpen = true;
            settingsPauseOpen = false;
            gameState = GameState.SETTINGS;
        }
    }
    public void closeSettings(){
        if (settingsMenuOpen){
            gameState = GameState.MENU;
            settingsMenuOpen = false;
        }
        if (settingsPauseOpen){
            gameState = GameState.PAUSED;
            settingsPauseOpen = false;
        }
    }
    public void returnToMenu() {
        gameState = GameState.MENU;
    }
    public void addAudioEvent(AudioEventType event){
        pendingAudioEvents.add(event);
    }
    //---------------------------------

    //UI SETTERS
    public void toggleSound(){
        if (soundEnabled){
            this.soundEnabled = false;
        }else{
            this.soundEnabled = true;
        }

    }
    public void toggleMusic(){
        if (musicEnabled){
            this.musicEnabled = false;
        }else{
            this.musicEnabled = true;
        }

    }

    public void setMinResolution(){
        this.resValue = 0;
    }
    public void setMidResolution(){
        this.resValue = 1;
    }
    public void setMaxResolution(){
        this.resValue = 2;
    }




    public void setPlayerColor(PlayerColor playerColor){
        this.playerColor = playerColor;
        if(player != null){
            player.setColor(playerColor);
        }
    }

    //---------------------------------

    //UI GETTERS
    public boolean isSoundEnabled(){
        return soundEnabled;
    }
    public boolean isMusicEnabled(){
        return musicEnabled;
    }
    public int getResolutionValue(){
        return resValue;
    }
    public PlayerColor getPlayerColor(){
        if(player != null){
            playerColor = player.getColor();
        }

        return playerColor;
    }

}
//-------------------------------------------------------------------------------------------------------------------
