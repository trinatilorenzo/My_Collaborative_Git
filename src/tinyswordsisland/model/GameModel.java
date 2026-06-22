package tinyswordsisland.model;

import tinyswordsisland.controller.InputState;
import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.config.GameConfig;
import tinyswordsisland.config.ObjConfig;
import tinyswordsisland.config.UIConfig;
import tinyswordsisland.config.enu.*;
import tinyswordsisland.model.entity.*;
import tinyswordsisland.model.event.AudioEventType;
import tinyswordsisland.model.object.*;
import tinyswordsisland.model.util.GameSettings;
import tinyswordsisland.model.util.GameSystem.*;
import tinyswordsisland.model.util.GameSystem.LevelInitializer.InitializedWorld;

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
    private transient EnemySystem enemySystem;
    private transient WorldObjectSystem worldObjectSystem;
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
    private MessageSystem messageSystem;
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
        enemySystem = new EnemySystem();
        worldObjectSystem = new WorldObjectSystem();

        gameState = GameState.MENU; // Default game state is the menu state

        settingsMenuOpen = false;
        settingsPauseOpen = false;
        settings = new GameSettings();

        messageSystem = new MessageSystem();

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

        messageSystem.clearAll();
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
        enemySystem.update(this, deltaMs);

        if (player.getState() == PlayerState.WALKING) {
            player.move();
        }

        worldObjectSystem.update(this, deltaMs);

        monk.update(player, deltaMs);
        messageSystem.update(this, input, deltaMs);
        interactionSystem.update(this);

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
        this.interactionSystem = new InteractionSystem();
        this.enemySystem = new EnemySystem();
        this.worldObjectSystem = new WorldObjectSystem();

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
        this.interactionSystem = new InteractionSystem();
        this.enemySystem = new EnemySystem();
        this.worldObjectSystem = new WorldObjectSystem();

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
        if (this.messageSystem == null) {
            this.messageSystem = new MessageSystem();
        } else {
            this.messageSystem.afterLoad();
        }
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
    public String getCurrentDialogue() {
        return messageSystem.getCurrentDialogue();
    }

    public String getCurrentMessage() {
        return messageSystem.getCurrentMessage();
    }
    public List<DynamiteProjectile> getProjectiles(){ return projectiles; }
    public List<EnemyTorch> getTorchEnemies() { return torchEnemies; }
    public GameConfig getGameConfig() { return gameConfig; }
    public boolean isCurrentLevelPowerUpCollected() {
        return currentLevelPowerUpCollected;
    }

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
    public void showMessage(String message) {
        messageSystem.showMessage(message);
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
