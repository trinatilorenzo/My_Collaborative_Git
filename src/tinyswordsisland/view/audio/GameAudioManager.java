package tinyswordsisland.view.audio;

import tinyswordsisland.model.enu.GameState;

public class GameAudioManager {

    // --- MUSIC ----
    private static final String MENU_MUSIC = "/res/Sound/Music/GameMusic00_cut.mp3";
    private static final String GAME_MUSIC = "/res/Sound/Music/GameMusic01.mp3";
    private static final String GAME_OVER_MUSIC = "/res/Sound/Music/GameMusic04.mp3";
    private static final String WIN_MUSIC = "/res/Sound/Music/GameMusic03.mp3";

    // --- SFX ----
    private static final String POWER_UP = "/res/Sound/SFX/power-up.mp3";
    private static final String LEVEL_UP = "/res/Sound/SFX/level-up.mp3";
    private static final String SWORD_SFX = "/res/Sound/SFX/sword-slash.mp3";
    private static final String SWORD_SWOOSH_SFX = "/res/Sound/SFX/sword-swoosh.mp3";
    private static final String TNT_EXPLOSION_SFX = "/res/Sound/SFX/tnt_explosion.mp3";
    private static final String TNT_ACTIVATION_SFX = "/res/Sound/SFX/tnt_activation.mp3";
    private static final String PLAYER_DAMAGE_SFX = "/res/Sound/SFX/player_damage_02.mp3";
    private static final String PLAYER_WALK_SFX = "/res/Sound/SFX/footsteps02.mp3";
    private static final String TREE_HIT_SFX = "/res/Sound/SFX/tree_hit.mp3";
    private static final String TREE_FINAL_SFX = "/res/Sound/SFX/tree_final.mp3";
    private static final String MOB_TALK = "/res/Sound/SFX/mob_talk.mp3";
    private static final String MOB_MAGIC = "/res/Sound/SFX/magic.mp3";
    private static final String ENEMY_HIT = "/res/Sound/SFX/pouch.mp3";
    private static final String ENEMY_DEAD = "/res/Sound/SFX/goblin-kill.mp3";

    private static final String PROJECTILE_LAUNCHED = "/res/Sound/SFX/goblin-sound02.mp3";
    private static final String PROJECTILE_EXPLOSION = "/res/Sound/SFX/dynamite_explosion.mp3";

    private static final String BUTTON_CLICKED = "/res/Sound/SFX/button_click.mp3";
    private static final String BUTTON_HOVER = "/res/Sound/SFX/button_hover.mp3";



    private final AudioPlayer musicPlayer = new AudioPlayer();
    private final AudioPlayer movementLoopPlayer = new AudioPlayer();

    private final AudioPlayer[] sfxPool = {
            new AudioPlayer(),
            new AudioPlayer(),
            new AudioPlayer(),
            new AudioPlayer(),
            new AudioPlayer(),
            new AudioPlayer()
    };

    private int curIndex = 0;
    private int nextSfxIndex = 0;
    private GameState currentState;

    public void syncBackgroundMusic(GameState newState) {

        switch (newState) {
            case WIN -> musicPlayer.playLoop(WIN_MUSIC);
            case GAME_OVER -> musicPlayer.playLoop(GAME_OVER_MUSIC);
            case MENU, SETTINGS -> musicPlayer.playLoop(MENU_MUSIC);
            case PLAYING, PAUSED -> musicPlayer.playLoop(GAME_MUSIC);

            default -> musicPlayer.stopAll();
        }
    }

    public void playEvents(AudioEffect effect) {
        if (effect == null) return;

        switch (effect) {
            case BUTTON_CLICKED -> nextSfxPlayer().playOnce(BUTTON_CLICKED);
            case BUTTON_HOVER -> nextSfxPlayer().playOnce(BUTTON_HOVER);

            case PLAYER_WALK_START ->  movementLoopPlayer.playLoop(PLAYER_WALK_SFX);
            case PLAYER_WALK_STOP ->  movementLoopPlayer.stopMusic();
            case PLAYER_ATTACK -> {
                nextSfxPlayer().playSequence(new String[]{
                        SWORD_SWOOSH_SFX
                        //SWORD_SFX
                });
            }
            case PLAYER_ATTACK_STOP -> currentSfxPlayer().stopAll();
            case PLAYER_DAMAGED -> nextSfxPlayer().playOnce(PLAYER_DAMAGE_SFX);

            case DIALOGUE_ADVANCE -> nextSfxPlayer().playOnce(MOB_TALK);
            case DIALOGUE_CLOSE -> nextSfxPlayer().playOnce(MOB_MAGIC);

            case TREE_HIT -> nextSfxPlayer().playOnce(TREE_HIT_SFX);
            case TREE_FINAL -> nextSfxPlayer().playOnce(TREE_FINAL_SFX);

            case TNT_TRIGGERED ->  nextSfxPlayer().playOnce(TNT_ACTIVATION_SFX);
            case TNT_EXPLOSION -> nextSfxPlayer().playOnce(TNT_EXPLOSION_SFX);
            case ENEMY_HIT -> nextSfxPlayer().playOnce(ENEMY_HIT);
            case ENEMY_DEFEATED -> nextSfxPlayer().playOnce(ENEMY_DEAD);
            case POWERUP_COLLECTED -> nextSfxPlayer().playOnce(POWER_UP);
            case LEVEL_UP -> nextSfxPlayer().playOnce(LEVEL_UP);

            case PROJECTILE_LAUNCHED -> nextSfxPlayer().playOnce(PROJECTILE_LAUNCHED);
            case PROJECTILE_EXPLODED -> nextSfxPlayer().playOnce(PROJECTILE_EXPLOSION);
        }

    }

    private AudioPlayer nextSfxPlayer() {
        curIndex = (nextSfxIndex) % sfxPool.length;

        AudioPlayer player = sfxPool[nextSfxIndex];
        nextSfxIndex = (nextSfxIndex + 1) % sfxPool.length;
        return player;
    }

    private AudioPlayer currentSfxPlayer() {
        return sfxPool[curIndex];
    }

    public void stopAll() {
        musicPlayer.stopAll();
        movementLoopPlayer.stopAll();

        for (AudioPlayer sfxPlayer : sfxPool) {
            sfxPlayer.stopAll();
        }

    }

    public void setMusicVolume(double volume) {
        musicPlayer.setMusicVolume(volume);
    }

    public void setSfxVolume(double volume) {
        movementLoopPlayer.setSfxVolume(volume);
        for (AudioPlayer sfxPlayer : sfxPool) {
            sfxPlayer.setSfxVolume(volume);
        }
    }

    public double getMusicVolume() {
        return musicPlayer.getMusicVolume();
    }

    public double getSfxVolume() {
        return movementLoopPlayer.getSfxVolume();
    }
}