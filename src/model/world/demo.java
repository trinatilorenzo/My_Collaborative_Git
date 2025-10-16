package model.world;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

// Rappresenta una regione (sorgente) dentro l'atlas
class TileRegion {
    final int sx, sy, sw, sh;
    TileRegion(int sx, int sy, int sw, int sh) {
        this.sx = sx; this.sy = sy; this.sw = sw; this.sh = sh;
    }
}

// Atlas organizzato a griglia (tileWidth x tileHeight) con margin/spacing opzionali
class Atlas {
    final BufferedImage image;
    final int tileW, tileH, margin, spacing, cols, rows;

    Atlas(BufferedImage image, int tileW, int tileH, int margin, int spacing) {
        this.image = image;
        this.tileW = tileW; this.tileH = tileH;
        this.margin = margin; this.spacing = spacing;
        int usableW = image.getWidth() - 2 * margin + spacing;
        int usableH = image.getHeight() - 2 * margin + spacing;
        this.cols = Math.max(0, usableW / (tileW + spacing));
        this.rows = Math.max(0, usableH / (tileH + spacing));
    }

    static Atlas load(String path, int tileW, int tileH, int margin, int spacing) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        return new Atlas(img, tileW, tileH, margin, spacing);
    }

    // id lineare: riga-major (0..cols*rows-1)
    TileRegion regionById(int id) {
        int x = id % cols;
        int y = id / cols;
        int sx = margin + x * (tileW + spacing);
        int sy = margin + y * (tileH + spacing);
        return new TileRegion(sx, sy, tileW, tileH);
    }

    // Disegna una regione alla destinazione (dx,dy,dw,dh)
    void draw(Graphics2D g, TileRegion r, int dx, int dy, int dw, int dh) {
        g.drawImage(image,
                dx, dy, dx + dw, dy + dh,
                r.sx, r.sy, r.sx + r.sw, r.sy + r.sh,
                null
        );
    }
}

// Animazione semplice basata su frame dell'atlas
class TileAnimation {
    final TileRegion[] frames;
    final int frameMs;
    int t = 0, idx = 0;

    TileAnimation(TileRegion[] frames, int frameMs) {
        this.frames = frames;
        this.frameMs = frameMs;
    }

    void update(int dtMs) {
        t += dtMs;
        while (t >= frameMs) {
            t -= frameMs;
            idx = (idx + 1) % frames.length;
        }
    }

    TileRegion current() { return frames[idx]; }
}

// Mappa di tile con indici nell'atlas
class TileMap {
    final int width, height, tileW, tileH;
    final int[][] tiles;

    TileMap(int width, int height, int tileW, int tileH) {
        this.width = width; this.height = height;
        this.tileW = tileW; this.tileH = tileH;
        this.tiles = new int[height][width];
    }

    void set(int x, int y, int tileId) { tiles[y][x] = tileId; }

    void draw(Graphics2D g, Atlas atlas, int offX, int offY, int scale) {
        int dw = tileW * scale, dh = tileH * scale;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileRegion r = atlas.regionById(tiles[y][x]);
                int dx = offX + x * dw;
                int dy = offY + y * dh;
                atlas.draw(g, r, dx, dy, dw, dh);
            }
        }
    }
}

// Esempio minimo Swing
class DemoPanel extends JPanel {
    Atlas atlas;
    TileMap map;
    TileAnimation waterAnim;
    long last = System.nanoTime();

    DemoPanel() throws IOException {
        // Esempio: atlas.png con tile 16x16, margin 2, spacing 2
        atlas = Atlas.load("atlas.png", 16, 16, 2, 2);
        map = new TileMap(20, 12, 16, 16);

        // Riempie la mappa con il tile 0, bordi con tile 1
        for (int y = 0; y < map.height; y++) {
            for (int x = 0; x < map.width; x++) {
                boolean border = (x == 0 || y == 0 || x == map.width - 1 || y == map.height - 1);
                map.set(x, y, border ? 1 : 0);
            }
        }

        // Animazione d’acqua: frame 10,11,12
        TileRegion f0 = atlas.regionById(10);
        TileRegion f1 = atlas.regionById(11);
        TileRegion f2 = atlas.regionById(12);
        waterAnim = new TileAnimation(new TileRegion[]{f0, f1, f2}, 150);

        Timer timer = new Timer(16, e -> {
            long now = System.nanoTime();
            int dt = (int)((now - last) / 1_000_000);
            last = now;
            waterAnim.update(dt);
            repaint();
        });
        timer.start();
        setPreferredSize(new Dimension(640, 384));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        try {
            // Disegna la mappa
            map.draw(g, atlas, 16, 16, 2);

            // Disegna un tile animato in overlay
            TileRegion cur = waterAnim.current();
            atlas.draw(g, cur, 400, 100, 32, 32);
        } finally {
            g.dispose();
        }
    }
}




