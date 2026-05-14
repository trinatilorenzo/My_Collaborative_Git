package view.audio;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioPlayer {
    private Thread musicThread;
    private volatile Player currentMusicPlayer;
    private String currentMusicPath;
    private volatile int musicSession = 0;

    public synchronized void playLoop(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return;

        if (resourcePath.equals(currentMusicPath) && musicThread != null && musicThread.isAlive()) {
            return;
        }

        stopMusic();
        currentMusicPath = resourcePath;

        int session = ++musicSession;
        musicThread = new Thread(() -> {
            while (session == musicSession) {
                playTrack(resourcePath, true, session);
            }
        }, "music-loop");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public void playOnce(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return;

        Thread sfxThread = new Thread(() -> playTrack(resourcePath, false, -1), "sfx-once");
        sfxThread.setDaemon(true);
        sfxThread.start();
    }

    public synchronized void stopMusic() {
        musicSession++;
        if (currentMusicPlayer != null) {
            currentMusicPlayer.close();
            currentMusicPlayer = null;
        }
        currentMusicPath = null;
        musicThread = null;
    }

    public void stopAll() {
        stopMusic();
    }

    private void playTrack(String resourcePath, boolean isMusic, int session) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Audio non trovato: " + resourcePath);
                return;
            }

            try (BufferedInputStream bis = new BufferedInputStream(is)) {
                Player player = new Player(bis);

                if (isMusic) {
                    if (session != musicSession) {
                        player.close();
                        return;
                    }
                    currentMusicPlayer = player;
                }

                player.play();
            }
        } catch (Exception e) {
            System.err.println("Errore audio su " + resourcePath + ": " + e.getMessage());
        } finally {
            if (isMusic && session == musicSession) {
                currentMusicPlayer = null;
            }
        }
    }
}
