package tinyswordsisland.view.audio;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;

public class AudioPlayer {

    private static final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    private Thread musicThread;
    private volatile Player musicPlayer;
    private volatile String currentMusicPath;
    private volatile int musicSession = 0;
    private volatile double musicVolume = 0.65;

    private Thread sfxThread;
    private volatile Player sfxPlayer;
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

        int session = ++sfxSession;

        sfxThread = new Thread(() -> play(path, false, session), "sfx-once");
        sfxThread.setDaemon(true);
        sfxThread.start();
    }

    public synchronized void stopMusic() {
        musicSession++;
        Player p = musicPlayer;
        musicPlayer = null;
        currentMusicPath = null;
        musicThread = null;
        if (p != null) { try { p.close(); } catch (Exception ignored) {} }
    }

    public synchronized void stopSfx() {
        sfxSession++;
        Player p = sfxPlayer;
        sfxPlayer = null;
        sfxThread = null;
        if (p != null) { try { p.close(); } catch (Exception ignored) {} }
    }

    public void stopAll() {
        stopMusic();
        stopSfx();
    }

    public void setMusicVolume(double v) { musicVolume = clamp(v); }
    public void setSfxVolume(double v)   { sfxVolume   = clamp(v); }
    public double getMusicVolume()       { return musicVolume; }
    public double getSfxVolume()         { return sfxVolume; }

    // -------------------------------------------------------------------------

    private void play(String path, boolean isMusic, int session) {
        byte[] data = loadBytes(path);
        if (data == null) return;

        try {
            DoubleSupplier vol = isMusic ? () -> musicVolume : () -> sfxVolume;
            VolumeAudioDevice device = new VolumeAudioDevice(vol);
            Player player = new Player(new ByteArrayInputStream(data), device);

            if (isMusic) {
                if (session != musicSession) { player.close(); return; }
                musicPlayer = player;
            } else {
                if (session != sfxSession) { player.close(); return; }
                sfxPlayer = player;
            }

            player.play();

        } catch (Exception e) {
            System.err.println("Audio error on " + path + ": " + e.getMessage());
        } finally {
            if (isMusic  && session == musicSession) musicPlayer = null;
            if (!isMusic && session == sfxSession)   sfxPlayer   = null;
        }
    }

    // -------------------------------------------------------------------------

    private byte[] loadBytes(String path) {
        byte[] cached = cache.get(path);
        if (cached != null) return cached;

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Audio not found: " + path);
                return null;
            }
            byte[] data = is.readAllBytes();
            byte[] existing = cache.putIfAbsent(path, data);
            return existing != null ? existing : data;
        } catch (Exception e) {
            System.err.println("Error loading audio " + path + ": " + e.getMessage());
            return null;
        }
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    // =========================================================
    //  AudioDevice con controllo volume via MASTER_GAIN
    // =========================================================

    private static final class VolumeAudioDevice extends JavaSoundAudioDevice {

        private static final Field SOURCE_FIELD = findSourceField();

        private final DoubleSupplier volumeSupplier;
        private volatile FloatControl gainControl;
        private volatile double lastAppliedVolume = -1.0;

        VolumeAudioDevice(DoubleSupplier volumeSupplier) {
            this.volumeSupplier = volumeSupplier;
        }

        @Override
        protected void createSource() throws JavaLayerException {
            super.createSource();
            bindGainControl();
            applyVolumeIfNeeded();
        }

        @Override
        protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
            applyVolumeIfNeeded();
            super.writeImpl(samples, offs, len);
        }

        private void bindGainControl() {
            gainControl = null;
            try {
                Object raw = SOURCE_FIELD != null ? SOURCE_FIELD.get(this) : null;
                if (!(raw instanceof SourceDataLine line)) return;
                if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                }
            } catch (Exception ignored) {}
        }

        private void applyVolumeIfNeeded() {
            FloatControl control = gainControl;
            if (control == null) return;

            double requested = clamp(volumeSupplier.getAsDouble());
            if (Math.abs(requested - lastAppliedVolume) < 0.001) return;
            lastAppliedVolume = requested;

            float minDb = control.getMinimum();
            float maxDb = control.getMaximum();
            float targetDb = requested <= 0.0001
                    ? minDb
                    : (float) (20.0 * Math.log10(requested));

            if (targetDb < minDb) targetDb = minDb;
            if (targetDb > maxDb) targetDb = maxDb;

            try { control.setValue(targetDb); } catch (IllegalArgumentException ignored) {}
        }

        private static Field findSourceField() {
            Class<?> type = JavaSoundAudioDevice.class;
            while (type != null) {
                try {
                    Field f = type.getDeclaredField("source");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            return null;
        }
    }
}