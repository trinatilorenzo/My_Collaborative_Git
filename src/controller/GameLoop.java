package controller;

// this class is a Thread that control the game update
// every n FPS

import main.GameSetting;

public class GameLoop extends Thread {
    private static final int FPS = 60;
    private final GameController controller;
    private boolean running = true;
    private GameSetting gs;

    public GameLoop(GameController controller, GameSetting settings) {
        this.controller = controller;
        this.gs = settings;
    }

    @Override
    public void run() {
        long currentTime;
        long lastTime = System.nanoTime();
        double drawInterval = 1e9/gs.FPS; // 0.01666 seconds;

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
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                lastFpsTime = currentTime;
            }
            //------------------------------------------------


            try {
                Thread.sleep(1); // alternative Thread.yield(); TO DO
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void stopGameLoop() {
        // stop Thread
        running = false;
    }
}