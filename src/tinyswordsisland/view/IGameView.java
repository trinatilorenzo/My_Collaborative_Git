package tinyswordsisland.view;

import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import tinyswordsisland.config.enu.GameState;
import tinyswordsisland.view.audio.AudioEffect;

public interface IGameView {

    void render();
    void updateAnimations(double deltaMs);
    void onGameStateChanged(GameState state);
    void shutdownAudio();
    void updatePlayerColor();
    void setResolution();
    void applyMenuState(GameState screen, Enum<?> hovered, Enum<?> selected);
    
    Enum<?> getButtonAtPoint(GameState state, Point point);

    void addKeyListener(KeyListener l);
    void addMouseListener(MouseListener l);
    void addMouseMotionListener(MouseMotionListener l);
    void setFocusable(boolean focusable);

    void playAudio(AudioEffect audio);
    void triggerDamageFlash();
}
