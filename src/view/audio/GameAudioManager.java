package view.audio;

import main.CONFIG.enu.GameState;
import model.event.AudioEventType;

import java.util.List;

import static model.event.AudioEventType.*;

public class GameAudioManager {
    private static final String MENU_MUSIC = "/res/Sound/Music/GameMusic00_cut.mp3";
    private static final String GAME_MUSIC = "/res/Sound/Music/GameMusic01.mp3";
    private static final String GAME_OVER_MUSIC = "/res/Sound/Music/GameMusic03.mp3";
    private static final String DEFAULT_SFX = "/res/Sound/SFX/SFX_game-bonus.mp3";
    private static final String SWORD_SFX = "/res/Sound/SFX/sword-slash.mp3";
    private static final String SWORD_REVERSE_SFX = "/res/Sound/SFX/sword-slash-reverse.mp3";
    private static final String SWORD_SWOOSH_SFX = "/res/Sound/SFX/sword-swoosh.mp3";
    private static final String TNT_EXPLOSION_SFX = "/res/Sound/SFX/tnt_explosion.mp3";
    private static final String TNT_ACTIVATION_SFX = "/res/Sound/SFX/tnt_activation.mp3";
    private static final String PLAYER_DAMAGE_SFX = "/res/Sound/SFX/player_damage_02.mp3";

    private final AudioPlayer player = new AudioPlayer();
    private GameState currentState;

    public void syncBackgroundMusic(GameState newState) {
        if (newState == null || newState == currentState) return;
        currentState = newState;

        if (newState == GameState.MENU) {
            player.playLoop(MENU_MUSIC);
            return;
        }
        if (newState == GameState.PLAYING || newState == GameState.PAUSED) {
            player.playLoop(GAME_MUSIC);
            return;
        }
        if (newState == GameState.GAME_OVER) {
            player.playLoop(GAME_OVER_MUSIC);
        }
    }

    public void playEvents(List<AudioEventType> events) {
        if (events == null || events.isEmpty()) return;

        for (AudioEventType eventType : events) {
            if (eventType.equals(PLAYER_ATTACK)){
                player.playSequence(new String[]{
                        SWORD_SWOOSH_SFX,
                        SWORD_SFX
                });
            }
            if(eventType.equals(PLAYER_ATTACK_STOP)){
                player.stopSfx();
                }
            if (eventType.equals(PLAYER_DAMAGED)){
                player.playOnce(PLAYER_DAMAGE_SFX);
            }
            if (eventType.equals(TNT_TRIGGERED)){
                player.playOnce(TNT_ACTIVATION_SFX);
            }
            if (eventType.equals(TNT_EXPLOSION)){
                player.playOnce(TNT_EXPLOSION_SFX);
            }

        }

    }

    public void stopAll() {
        player.stopAll();
    }

    public void setMusicVolume(double volume) {
        player.setMusicVolume(volume);
    }

    public void setSfxVolume(double volume) {
        player.setSfxVolume(volume);
    }

    public double getMusicVolume() {
        return player.getMusicVolume();
    }

    public double getSfxVolume() {
        return player.getSfxVolume();
    }
}
