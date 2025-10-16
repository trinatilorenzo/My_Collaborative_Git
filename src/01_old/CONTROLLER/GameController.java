package old.CONTROLLER;

import old.GameSetting;
import old.MODEL.GameModel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameController implements KeyListener {
    // ALL THE CONTROLLER STAFF HERE
    // input, game loop , system ...

    private final GameModel model;
    private GameSetting gs;

    private volatile boolean running = false;

    public GameController(GameModel model, GameSetting gs) {
        this.model = model;
        this.gs = gs;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> model.player.up();
            case KeyEvent.VK_DOWN -> model.player.down();
            case KeyEvent.VK_LEFT -> model.player.left();
            case KeyEvent.VK_RIGHT -> model.player.right();
        }
        view.repaint();
        System.out.println("Event " + e);
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    // GAME THREAD LOOP
    //-----------------------------------------------------------------------------------
    public void startGameLoop() {
        running = true;
        Thread gameThread = new Thread(() -> {
            long currentTime;
            long lastTime = System.nanoTime();

            // debug
            int drawCount = 0;
            long lastFpsTime = System.nanoTime();
            //------------------------------------------------

            double drawInterval = 1e9/gs.FPS; // 0.01666 seconds;

            while (running) {
                currentTime = System.nanoTime();

                if (currentTime - lastTime >= drawInterval) {
                    //model.update();       // update game status
                    view.repaint();       // update visual graphics
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
        }, "Game Loop Thread");

        // start thread
        gameThread.start();
    }

    public void stopGameLoop() {
        // stop Thread
        running = false;
    }

    //-----------------------------------------------------------------------------------
}
