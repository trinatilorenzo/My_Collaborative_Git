package view.audio;

import main.CONFIG.enu.GameState;
import model.event.AudioEventType;

import java.util.List;

import static model.event.AudioEventType.*;

public class GameAudioManager {

    private static final String MENU_MUSIC = "/res/Sound/Music/GameMusic00_cut.mp3";
    private static final String GAME_MUSIC = "/res/Sound/Music/GameMusic01.mp3";
    private static final String GAME_OVER_MUSIC = "/res/Sound/Music/GameMusic03.mp3";

    private static final String SWORD_SFX = "/res/Sound/SFX/sword-slash.mp3";
    private static final String SWORD_SWOOSH_SFX = "/res/Sound/SFX/sword-swoosh.mp3";
    private static final String TNT_EXPLOSION_SFX = "/res/Sound/SFX/tnt_explosion.mp3";
    private static final String TNT_ACTIVATION_SFX = "/res/Sound/SFX/tnt_activation.mp3";
    private static final String PLAYER_DAMAGE_SFX = "/res/Sound/SFX/player_damage_02.mp3";
    private static final String PLAYER_WALK_SFX = "/res/Sound/SFX/footsteps.mp3";
    private static final String TREE_HIT_SFX = "/res/Sound/SFX/tree_hit.mp3";
    private static final String TREE_FINAL_SFX = "/res/Sound/SFX/tree_final.mp3";


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
        if (newState == null || newState == currentState) return;
        currentState = newState;

        if (newState != GameState.PLAYING) {
            movementLoopPlayer.stopSfx();
        }

        if (newState == GameState.MENU) {
            musicPlayer.playLoop(MENU_MUSIC);
            return;
        }

        if (newState == GameState.PLAYING || newState == GameState.PAUSED) {
            musicPlayer.playLoop(GAME_MUSIC);
            return;
        }

        if (newState == GameState.GAME_OVER) {
            musicPlayer.playLoop(GAME_OVER_MUSIC);
        }

    }

    public void playEvents(List<AudioEventType> events) {
        if (events == null || events.isEmpty()) return;

        for (AudioEventType eventType : events) {

            if (eventType == PLAYER_ATTACK) {
                nextSfxPlayer().playSequence(new String[]{
                        SWORD_SWOOSH_SFX,
                        SWORD_SFX
                });
            }
            if (eventType == PLAYER_ATTACK_STOP){
                currentSfxPlayer().stopAll();
            }

            if (eventType == PLAYER_DAMAGED) {
                nextSfxPlayer().playOnce(PLAYER_DAMAGE_SFX);
            }

            if (eventType == TNT_TRIGGERED) {
                nextSfxPlayer().playOnce(TNT_ACTIVATION_SFX);
            }

            if (eventType == TNT_EXPLOSION) {
                nextSfxPlayer().playOnce(TNT_EXPLOSION_SFX);
            }

            if (eventType == PLAYER_WALK_START) {
                movementLoopPlayer.playLoop(PLAYER_WALK_SFX);
            }

            if (eventType == PLAYER_WALK_STOP) {
                    movementLoopPlayer.stopMusic();
            }
            if(eventType == TREE_HIT){
                nextSfxPlayer().playOnce(TREE_HIT_SFX);
            }
            if(eventType == TREE_FINAL){
                nextSfxPlayer().playOnce(TREE_FINAL_SFX);
            }
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