package controller;

import java.util.concurrent.locks.LockSupport;
import static main.GameSetting.*;

// GAME LOOP CLAS
// This class is a Thread that control the game update every n FPS
//-------------------------------------------------------------------------------------------------------------------
public class GameLoop extends Thread {
    private final GameController controller;
    private boolean running = true;

    public GameLoop(GameController controller) {
        this.controller = controller;
        this.setPriority(Thread.MAX_PRIORITY);
    }


    // GAME LOOP CORE --> START()

    /**
     * Il loop è progettato per far sì che la logica di gioco avanzi sempre a passi fissi e regolari,
     * mentre il render si adatta a quanti frame "reali" sono passati.
     */
    //-------------------------------------------------------------
    //TODO capire bene come funziona
    @Override
    public void run() {
        long drawInterval = (long)(1e9 / FPS); // ideal duration of a frame
        double fixedDeltaMs = drawInterval / 1e7;
        long lastTime = System.nanoTime(); // when the last frame was drawn

        long lastFpsTime = lastTime; // when the last FPS was printed
        int drawCount = 0; // count frames drawn for debug

        while (running) {
            // CORE CICLING LOOP (COLOCK TICK)
            long currentTime = System.nanoTime(); // current time
            long deltaNs = currentTime - lastTime; // time since last frame
            int catchUp = 0;

            while (deltaNs >= drawInterval && catchUp < MAX_FRAME_SKIP) {
                // INNER LOOP (Delay catch up)
                controller.update(fixedDeltaMs);

                lastTime += drawInterval;
                deltaNs = currentTime - lastTime;
                catchUp++;
            }
            controller.render();

            // debug console
            drawCount++;
            if (currentTime - lastFpsTime >= 1_000_000_000L) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                lastFpsTime += 1_000_000_000L;
            }
            //-----------------

            long sleepNs = drawInterval - (System.nanoTime() - lastTime);
            if (sleepNs > 300_000) {
                LockSupport.parkNanos(sleepNs - 200_000);
                long spinUntil = System.nanoTime() + 200_000;
                while (System.nanoTime() < spinUntil) Thread.onSpinWait();
            } else if (sleepNs > 0) {
                long spinUntil = System.nanoTime() + sleepNs;
                while (System.nanoTime() < spinUntil) Thread.onSpinWait();
            } else {
                Thread.yield();
            }
        }
    }

    //-------------------------------------------------------------

    // STOP GameLoop()
    public void stopGameLoop() {
        // stop Thread
        running = false;
    }
    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
