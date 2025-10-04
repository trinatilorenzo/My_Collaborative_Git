package main;

import tile.TileManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import entity.Player;



public class GamePanel extends JPanel implements Runnable{

    static final int MUSIC_GAME_INDEX = 0;

    // SCREEN SETTINGS
    //-----------------------------------------------------------------------
    final int originalTileSize = 16; // 16x16 tile
    final int scale = 3;

    public final int tileSize = originalTileSize * scale; // 48x48 tile
    public final int maxScreenCol = 12;
    public final int maxScreenRow = 16;
    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels
    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels
    //-----------------------------------------------------------------------

    // WORLD SETTINGS
    //-----------------------------------------------------------------------
    public final int maxWorldCol = 12;
    public final int maxWorldRow = 16;
    //-----------------------------------------------------------------------

    // FPS
    final int FPS = 60;

    //SYSTEM
    //-----------------------------------------------------------------------
    TileManager tileM = new TileManager(this);
    KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    //-----------------------------------------------------------------------

    // ENTITY AND OBJECT
    //-----------------------------------------------------------------------
    Player player = new Player(this, keyH);
    //-----------------------------------------------------------------------

    public GamePanel() {
        this.setPreferredSize (new Dimension(screenWidth, screenHeight)) ;
        this.setBackground (Color.black) ;
        this.setDoubleBuffered (true) ;
        this.addKeyListener(keyH);
        this.setFocusable(true);
    }

    public void setupGame(){
        //aSetter.setObject();
        // playMusic(MUSIC_GAME_INDEX);
    }

    public void startGameThread(){
        // use a specific thread to handle the game loop
        gameThread = new Thread(this);
        gameThread.start();
    }

    // GAME LOOP
    @Override
    public void run() {

        double drawInterval = 1e9/FPS; // 0.01666 seconds;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        // GAME LOOP
        while(gameThread != null){

            currentTime = System.nanoTime();
            delta += (currentTime-lastTime) / drawInterval;
            timer += (currentTime-lastTime);
            lastTime = currentTime;

            if (delta >= 1){
                update();
                repaint();

                delta--;
                drawCount++;
            }

            if (timer >= 1e9 ){
                System.out.println(("FPS:" + drawCount));
                drawCount = 0;
                timer = 0;
            }


        }
    }

    public void update(){
        player.update();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        // DEBUG
        long drawStart = 0;
        if (keyH.checkDrawTime){
            drawStart = System.nanoTime();
        }

        //---------------------------------
        // TILE
        //---------------------------------

        tileM.draw(g2);

        //---------------------------------
        // OBJECT
        //---------------------------------

/*
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                obj[i].draw(g2, this);
            }
        }
*/
        //---------------------------------
        // PLAYER
        player.draw(g2);
        //---------------------------------


        // UI
        //ui.draw(g2);

        // DEBUG
        if (keyH.checkDrawTime){
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            g2.setColor(Color.white);
            g2.drawString("Draw time: "+ passed, 10 , 400);
            System.out.println("Draw time: "+ passed);
        }


        g2.dispose();
    }
    //---------------------------------
    // MUSIC
    //---------------------------------
    /*public void playMusic(int i){
        sound.setFile(i);
        sound.play();
        sound.loop();
    }

    public void stopMusic(){
        sound.stop();
    }

    public void playSE(int i){
        sound.setFile(i);
        sound.play();
    }*/
}
