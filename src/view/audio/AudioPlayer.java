package view.audio;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

public class AudioPlayer {
    private Thread musicThread;
    private volatile Player currentMusicPlayer;
    private String currentMusicPath;
    private volatile int musicSession = 0;

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
                playTrack(resourcePath, true, session);
            }
        }, "music-loop");
        musicThread.setDaemon(true);
        musicThread.start();
    }

    // =========================================================
    //  SFX ONE-SHOT
    // =========================================================

    public void playOnce(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) return;

        Thread sfxThread = new Thread(() -> playTrack(resourcePath, false, -1), "sfx-once");
        sfxThread.setDaemon(true);
        sfxThread.start();
    }

    // =========================================================
    //  SEQUENZA AUTOMATICA
    //  Riproduce tutti i suoni uno dopo l'altro senza intervento esterno.
    // =========================================================

    /**
     * Avvia la riproduzione automatica dell'array di suoni in ordine.
     * Ogni traccia parte solo quando la precedente è terminata.
     * Non bloccante: il lavoro viene svolto su un thread dedicato.
     */
    public void playSequence(String[] resourcePaths) {
        if (resourcePaths == null || resourcePaths.length == 0) return;

        Thread seqThread = new Thread(() -> {
            for (String path : resourcePaths) {
                if (path == null || path.isBlank()) continue;
                // playTrack è sincrono: ritorna solo a fine riproduzione
                playTrack(path, false, -1);
            }
        }, "sfx-sequence-auto");

        seqThread.setDaemon(true);
        seqThread.start();
    }

    // =========================================================
    //  SEQUENZA CONTROLLATA
    //  Carichi la sequenza una volta, poi chiami playNext()
    //  ogni volta che vuoi avanzare al suono successivo.
    // =========================================================

    /**
     * Carica una sequenza di suoni da riprodurre in modo controllato.
     * Resetta l'indice e annulla l'eventuale attesa in corso.
     */
    public void loadSequence(String[] resourcePaths) {
        if (resourcePaths == null) return;
        sequencePaths = resourcePaths.clone();
        sequenceIndex = 0;
        // latch già a 0 → nessuna traccia in corso, playNext() non aspetta
        trackLatch = new CountDownLatch(0);
    }

    /**
     * @return true se ci sono ancora tracce da riprodurre nella sequenza caricata.
     */
    public boolean hasNext() {
        return sequencePaths != null && sequenceIndex < sequencePaths.length;
    }

    /**
     * Aspetta che la traccia corrente (se presente) sia terminata,
     * poi avvia la successiva nella sequenza caricata con {@link #loadSequence}.
     *
     * @return true se una nuova traccia è stata avviata, false se la sequenza è esaurita.
     * @throws InterruptedException se il thread viene interrotto durante l'attesa.
     */
    public boolean playNext() throws InterruptedException {
        if (!hasNext()) return false;

        // Aspetta la fine della traccia precedente
        trackLatch.await();

        // Ricontrollo dopo l'attesa (la sequenza potrebbe essere stata resettata)
        if (!hasNext()) return false;

        String path = sequencePaths[sequenceIndex++];

        // Nuovo latch: verrà abbassato quando questa traccia finisce
        CountDownLatch latch = new CountDownLatch(1);
        trackLatch = latch;

        Thread t = new Thread(() -> {
            playTrack(path, false, -1);
            latch.countDown(); // segnala che la traccia è finita
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

    public void stopAll() {
        stopMusic();
    }

    // =========================================================
    //  CORE (sincrono, blocca fino a fine riproduzione)
    // =========================================================

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

                player.play(); // blocca fino a fine traccia
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