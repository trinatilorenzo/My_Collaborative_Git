package tinyswordsisland.model;

import tinyswordsisland.controller.InputState;
import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.ObjConfig;
import tinyswordsisland.config.SpawnPoint;
import tinyswordsisland.config.UIConfig;
import tinyswordsisland.config.enu.*;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.*;
import tinyswordsisland.model.util.GameSettings;
import tinyswordsisland.model.util.InteractionSystem;
import tinyswordsisland.model.util.LevelInitializer;
import tinyswordsisland.model.util.LevelInitializer.InitializedWorld;

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
public class GameModel implements Serializable, IGameModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient GameConfig gameConfig;
    private transient CollisionChecker collisionChecker;
    private transient InteractionSystem interactionSystem;
    //-------------------------------------------------------------

    // Game status
    private GameState gameState;

    private int currentLevel;
    private boolean levelCompleted;
    private boolean currentLevelPowerUpCollected;
    //-------------------------------------------------------------


    // Map & OBJ
    private  GameMap worldGameMap;
    private List<GameObject> objects;
    //-------------------------------------------------------------

    // Player & NPC
    private Player player;
    private Monk monk;
    private List<EnemyTNT> tntEnemies;
    private List<EnemyDynamite> dynamiteEnemies;
    private List<DynamiteProjectile> projectiles;
    private List<EnemyTorch> torchEnemies;
    //-------------------------------------------------------------

    //UI State
    private boolean settingsMenuOpen, settingsPauseOpen;
    private GameSettings settings;
    //-------------------------------------------------------------

    // Dialogue
    private String currentDialogue; // dialogue currently displayed to the player
    private String currentMessage;
    private double messageTimer; //TODO forse da rimuovere
    //-------------------------------------------------------------

    // Death sequence
    private double deadStateElapsedMs; // TODO da spostare in view ?

    //Audio events
    private transient List<AudioEventType> pendingAudioEvents = new ArrayList<>(); // TODO da spostare in view



    //-------------------------------------------------------------
    /**
     * CONSTRUCTOR
      */
    //-------------------------------------------------------------
    public GameModel(GameConfig GS) {
        gameConfig = GS;
        collisionChecker = new CollisionChecker(this);
        interactionSystem = new InteractionSystem();

        gameState = GameState.MENU; // Default game state is the menu state

        settingsMenuOpen = false;
        settingsPauseOpen = false;
        settings = new GameSettings();

    }
    //-------------------------------------------------------------

    /**
     * Start a new game from scratch
     */
    //-------------------------------------------------------------
    @Override
    public void initializeNewGame() {
        InitializedWorld world = LevelInitializer.createNewWorld(gameConfig, settings.getPlayerColor());
        worldGameMap = world.worldGameMap();
        player = world.player();
        monk = world.monk();
        tntEnemies = world.tntEnemies();
        dynamiteEnemies = world.dynamiteEnemies();
        projectiles = world.projectiles();
        torchEnemies = world.torchEnemies();
        objects = world.objects();

        currentLevelPowerUpCollected = false;
        currentLevel = 0;
        levelCompleted = false;

        currentDialogue = "";
        currentMessage = "";
        messageTimer = 0.0;
        deadStateElapsedMs = 0.0;

        gameState = GameState.PLAYING;
    }
    //-------------------------------------------------------------------

    /**
     * MAIN METHOD OF THE CLASS
     * Update the tinyswordsisland.model status, Called by the tinyswordsisland.controller every frame
     */
    //-------------------------------------------------------------
    @Override
    public void update(InputState input, double deltaMs) {
        switch (gameState) {
            case MENU, SETTINGS, GAME_OVER, WIN -> { } //no update needed
            case PLAYING -> updatePlayingState(input, deltaMs);
            case PAUSED -> updateState(input);
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
        interactionSystem.update(this);
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
        }
        if (playerStateBeforeUpdate == PlayerState.WALKING && player.getState() != PlayerState.WALKING) {
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

    private void updateMessage(double deltaMS){

        if (currentMessage.isEmpty()) return;

        messageTimer += deltaMS;
        if (messageTimer >= UIConfig.MESSAGE_TIMER_MS){
            currentMessage="";
            messageTimer = 0;
        }

    }
    //----------------------------------------------------------------------


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
            gameState = GameState.GAME_OVER;
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

    public int getCurrentLevel() { return currentLevel; }
    public boolean isLevelCompleted() { return levelCompleted; }

    public List<IRenderable> getAllRenderables(){
        List<IRenderable> allRenderables = new ArrayList<>();

        // Entities
        if (player != null) { allRenderables.add(player);}
        if (monk != null) { allRenderables.add(monk);}
        if (tntEnemies != null) { allRenderables.addAll(tntEnemies);}
        if (torchEnemies != null) { allRenderables.addAll(torchEnemies);}
        if (dynamiteEnemies != null) { allRenderables.addAll(dynamiteEnemies);}
        if (projectiles != null) { allRenderables.addAll(projectiles);}


        // Objects
        if (objects != null) {allRenderables.addAll(objects); }
        return allRenderables;
    }
    @Override
    public Player getPlayer() {return player; }
    public GameMap getWorldMap() { return worldGameMap; }
    public CollisionChecker getCollisionChecker() { return collisionChecker;}
    public GameState getGameState() { return gameState; }
    public List<GameObject> getObjects() { return objects; }

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

    public void setCurrentLevelPowerUpCollected(boolean currentLevelPowerUpCollected) {
        this.currentLevelPowerUpCollected = currentLevelPowerUpCollected;
    }
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    public void setCurrentLevel(int currentLevel){
        this.currentLevel = currentLevel;
    }
    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }
    public void setLevelCompleted(boolean levelCompleted) {
        this.levelCompleted = levelCompleted;
    }


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
    public void setDebugMode(boolean debugMode) {
        settings.setDebugMode(debugMode);
    }
    public void toggleSound(){
        settings.toggleSound();
    }
    public void toggleMusic(){
        settings.toggleMusic();
    }
    public void setMinResolution(){
        settings.setMinResolution();
    }
    public void setMidResolution(){
        settings.setMidResolution();
    }
    public void setMaxResolution(){
        settings.setMaxResolution();
    }
    public void setPlayerColor(PlayerColor playerColor){
        settings.setPlayerColor(playerColor);
    }
    //---------------------------------

    //UI GETTERS
    public boolean isDebugMode() {
        return settings.isDebugMode();
    }
    public boolean isSoundEnabled(){
        return settings.isSoundEnabled();
    }
    public boolean isMusicEnabled(){
        return settings.isMusicEnabled();
    }
    public int getResolutionValue(){
        return settings.getResolutionValue();
    }
    public PlayerColor getPlayerColor(){
        return settings.getPlayerColor();
    }

}
//-------------------------------------------------------------------------------------------------------------------
