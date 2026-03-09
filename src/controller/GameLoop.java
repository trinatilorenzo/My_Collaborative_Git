package controller;

import static main.GameSetting.*;

// GAME LOOP CLAS
// This class is a Thread that control the game update every n FPS
//-------------------------------------------------------------------------------------------------------------------
public class GameLoop extends Thread {
    private final GameController controller;
    private boolean running = true;

    public GameLoop(GameController controller) {
        this.controller = controller;
    }

    // GAME LOOP CORE --> START()
    //-------------------------------------------------------------
    @Override
    public void run() {
        long currentTime;
        long lastTime = System.nanoTime();
        double drawInterval = 1e9/FPS; // 0.01666 seconds;

        // debug
        int drawCount = 0;
        long lastFpsTime = System.nanoTime();
        //------------------------------------------------

        while (running) {
            currentTime = System.nanoTime();
            if (currentTime - lastTime >= drawInterval) {

                controller.update();
                controller.render();

                lastTime = currentTime;

                // debug
                drawCount++;
                //------------------------------------------------
            }
            // debug
            if (currentTime - lastFpsTime >= 1e9) {
                //System.out.println("FPS: " + drawCount);
                drawCount = 0;
                lastFpsTime = currentTime;
            }
            //------------------------------------------------


            try {
                //TODO alternative Thread.yield()
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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