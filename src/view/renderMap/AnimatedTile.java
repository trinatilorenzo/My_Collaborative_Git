package view.renderMap;

// ANIMATED TILE CLASS
// This class manages the animation of tiles that have multiple frames (like water, etc.)
//-------------------------------------------------------------------------------------------------------------------
public class AnimatedTile {
    private int[] frameIds;          // array degli ID dei frame (es. [10, 11, 12, 13])
    private int currentFrameIndex;   // indice del frame corrente nell'array
    private double frameDurationMs;
    private long lastUpdateTime;

    public AnimatedTile(int[] frameIds, double frameDurationMs) {
        this.frameIds = frameIds;
        this.frameDurationMs = frameDurationMs;
        this.currentFrameIndex = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Aggiorna l'animazione: incrementa il contatore e avanza al frame successivo se necessario
     */
    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime >= frameDurationMs) {
            lastUpdateTime += frameDurationMs;
            currentFrameIndex++;
            if (currentFrameIndex >= frameIds.length) {
                currentFrameIndex = 0;
            }
        }
    }

    public int getCurrentFrameId() {
        return frameIds[currentFrameIndex];
    }

    public int getBaseId() {
        return frameIds[0];
    }
}
//-------------------------------------------------------------------------------------------------------------------
