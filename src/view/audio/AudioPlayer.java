package view.audio;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.function.DoubleSupplier;

public class AudioPlayer {
    private Thread musicThread;
    private volatile Player currentMusicPlayer;
    private String currentMusicPath;
    private volatile int musicSession = 0;
    private volatile double musicVolume = 0.65;
    private volatile double sfxVolume = 0.4;

    private Thread currentSfxThread;
    private volatile Player currentSfxPlayer;
    private volatile int sfxSession = 0;

    // --- Stato per la sequenza controllata ---
    private volatile String[] sequencePaths;
    private volatile int sequenceIndex;
    private volatile CountDownLatch trackLatch = new CountDownLatch(0);

    // =========================================================
    //  MUSIC LOOP
    // =========================================================

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
                playTrack(resourcePath, true, session, () -> musicVolume);
            }
        }, "music-loop");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    // =========================================================
    //  SFX ONE-SHOT
    // =========================================================

    public synchronized void playOnce(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return;

        stopSfx();

        int session = ++sfxSession;
        currentSfxThread = new Thread(
                () -> playTrack(resourcePath, false, session, () -> sfxVolume),
                "sfx-once"
        );
        currentSfxThread.setDaemon(true);
        currentSfxThread.start();
    }

    // =========================================================
    //  SEQUENZA AUTOMATICA
    // =========================================================

    public synchronized void playSequence(String[] resourcePaths) {
        if (resourcePaths == null || resourcePaths.length == 0) return;

        stopSfx();
        int session = ++sfxSession;

        currentSfxThread = new Thread(() -> {
            for (String path : resourcePaths) {
                if (path == null || path.isBlank()) continue;
                if (session != sfxSession) return;
                playTrack(path, false, session, () -> sfxVolume);
            }
        }, "sfx-sequence");

        currentSfxThread.setDaemon(true);
        currentSfxThread.start();
    }

    // =========================================================
    //  SEQUENZA CONTROLLATA
    // =========================================================

    public void loadSequence(String[] resourcePaths) {
        if (resourcePaths == null) return;
        sequencePaths = resourcePaths.clone();
        sequenceIndex = 0;
        trackLatch = new CountDownLatch(0);
    }

    public boolean hasNext() {
        return sequencePaths != null && sequenceIndex < sequencePaths.length;
    }

    public boolean playNext() throws InterruptedException {
        if (!hasNext()) return false;
        trackLatch.await();
        if (!hasNext()) return false;

        String path = sequencePaths[sequenceIndex++];

        CountDownLatch latch = new CountDownLatch(1);
        trackLatch = latch;

        Thread t = new Thread(() -> {
            playTrack(path, false, -1, () -> sfxVolume);
            latch.countDown();
        }, "sfx-seq-step");
        t.setDaemon(true);
        t.start();

        return true;
    }

    // =========================================================
    //  STOP
    // =========================================================

    public synchronized void stopMusic() {
        musicSession++;
        if (currentMusicPlayer != null) {
            currentMusicPlayer.close();
            currentMusicPlayer = null;
        }
        currentMusicPath = null;
        musicThread = null;
    }

    public synchronized void stopSfx() {
        sfxSession++;
        if (currentSfxPlayer != null) {
            currentSfxPlayer.close();
            currentSfxPlayer = null;
        }
        currentSfxThread = null;
    }

    public void stopAll() {
        stopMusic();
        stopSfx();
    }

    // =========================================================
    //  VOLUME
    // =========================================================

    public void setMusicVolume(double volume) {
        musicVolume = clampVolume(volume);
    }

    public void setSfxVolume(double volume) {
        sfxVolume = clampVolume(volume);
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    // =========================================================
    //  CORE (sincrono, blocca fino a fine riproduzione)
    // =========================================================

    private void playTrack(String resourcePath, boolean isMusic, int session, DoubleSupplier volumeSupplier) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Audio non trovato: " + resourcePath);
                return;
            }

            try (BufferedInputStream bis = new BufferedInputStream(is)) {
                VolumeAudioDevice audioDevice = new VolumeAudioDevice(volumeSupplier);
                Player player = new Player(bis, audioDevice);

                if (isMusic) {
                    if (session != musicSession) {
                        player.close();
                        return;
                    }
                    currentMusicPlayer = player;
                } else {
                    if (session != -1 && session != sfxSession) {
                        player.close();
                        return;
                    }
                    if (session != -1) {
                        currentSfxPlayer = player;
                    }
                }

                player.play();
            }
        } catch (Exception e) {
            System.err.println("Errore audio su " + resourcePath + ": " + e.getMessage());
        } finally {
            if (isMusic && session == musicSession) {
                currentMusicPlayer = null;
            }

            if (!isMusic && session != -1 && session == sfxSession) {
                currentSfxPlayer = null;
            }
        }
    }

    private static double clampVolume(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }

    /**
     * AudioDevice JLayer con controllo volume tramite MASTER_GAIN.
     * Se la line non espone il controllo, la riproduzione continua comunque.
     */
    private static final class VolumeAudioDevice extends JavaSoundAudioDevice {
        private static final Field SOURCE_FIELD;

        static {
            try {
                SOURCE_FIELD = JavaSoundAudioDevice.class.getDeclaredField("source");
                SOURCE_FIELD.setAccessible(true);
            } catch (Exception e) {
                throw new IllegalStateException("Impossibile inizializzare il controllo volume", e);
            }
        }

        private final DoubleSupplier volumeSupplier;
        private FloatControl gainControl;
        private double lastAppliedVolume = -1.0;

        private VolumeAudioDevice(DoubleSupplier volumeSupplier) {
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
            try {
                SourceDataLine sourceLine = (SourceDataLine) SOURCE_FIELD.get(this);
                if (sourceLine != null && sourceLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    gainControl = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
                }
            } catch (Exception ignored) {
                gainControl = null;
            }
        }

        private void applyVolumeIfNeeded() {
            if (gainControl == null) return;

            double requested = clampVolume(volumeSupplier.getAsDouble());
            if (Math.abs(requested - lastAppliedVolume) < 0.001) return;

            lastAppliedVolume = requested;

            float minDb = gainControl.getMinimum();
            float maxDb = gainControl.getMaximum();

            float targetDb = requested <= 0.0001
                    ? minDb
                    : (float) (20.0 * Math.log10(requested));

            if (targetDb < minDb) targetDb = minDb;
            if (targetDb > maxDb) targetDb = maxDb;

            gainControl.setValue(targetDb);
        }
    }
}