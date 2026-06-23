package tinyswordsisland.view.audio;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AudioPlayer {

    private static final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    private Thread musicThread;
    private volatile AdvancedPlayer musicPlayer;
    private volatile String currentMusicPath;
    private volatile int musicSession = 0;
    private volatile double musicVolume = 0.65;

    private Thread sfxThread;
    private volatile AdvancedPlayer sfxPlayer;
    private volatile int sfxSession = 0;
    private volatile double sfxVolume = 0.4;


    // -------------------------------------------------------------------------

    public synchronized void playLoop(String path) {
        if (path == null || path.isBlank()) return;
        if (path.equals(currentMusicPath) && musicThread != null && musicThread.isAlive()) return;

        stopMusic();
        currentMusicPath = path;
        int session = ++musicSession;

        musicThread = new Thread(() -> {
            while (session == musicSession) {
                play(path, true, session);
            }
        }, "music-loop");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public synchronized void playOnce(String path) {
        if (path == null || path.isBlank()) return;

        stopSfx();
        int session = ++sfxSession;

        sfxThread = new Thread(() -> play(path, false, session), "sfx-once");
        sfxThread.setDaemon(true);
        sfxThread.start();
    }

    public synchronized void stopMusic() {
        musicSession++;
        if (musicPlayer != null) { musicPlayer.stop(); musicPlayer = null; }
        currentMusicPath = null;
        musicThread = null;
    }

    public synchronized void stopSfx() {
        sfxSession++;
        if (sfxPlayer != null) { sfxPlayer.stop(); sfxPlayer = null; }
        sfxThread = null;
    }

    public void stopAll() {
        stopMusic();
        stopSfx();
    }

    public void setMusicVolume(double v) { musicVolume = clamp(v); }
    public void setSfxVolume(double v)   { sfxVolume   = clamp(v); }
    public double getMusicVolume() { return musicVolume; }
    public double getSfxVolume() { return sfxVolume; }





    // -------------------------------------------------------------------------

    private void play(String path, boolean isMusic, int session) {
        byte[] data = loadBytes(path);
        if (data == null) return;

        try {
            AdvancedPlayer player = new AdvancedPlayer(new ByteArrayInputStream(data));


            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackStarted(PlaybackEvent e) {
                }
                @Override
                public void playbackFinished(PlaybackEvent e) { }
            });

            if (isMusic) {
                if (session != musicSession) return;
                musicPlayer = player;
            } else {
                if (session != sfxSession) return;
                sfxPlayer = player;
            }

            player.play();

        } catch (Exception e) {
            System.err.println("Audio error on " + path + ": " + e.getMessage());
        } finally {
            if (isMusic)  musicPlayer = null;
            else          sfxPlayer   = null;
        }
    }

    // -------------------------------------------------------------------------


    /**
     * Cache load a single time in RAM
     */
    // -------------------------------------------------------------------------

    private byte[] loadBytes(String path) {
        return cache.computeIfAbsent(path, p -> {
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is == null) {
                    System.err.println("Audio not found: " + p);
                    return null;
                }
                return is.readAllBytes();
            } catch (Exception e) {
                System.err.println("Error loading audio " + p + ": " + e.getMessage());
                return null;
            }
        });
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}