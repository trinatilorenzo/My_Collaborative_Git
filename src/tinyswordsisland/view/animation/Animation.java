package tinyswordsisland.view.animation;

import java.awt.image.BufferedImage;

/**
 * ANIMATION CLASS
 * represent an animation made by multiple frames from a spritesheet
 */

//-------------------------------------------------------------------------------------------------------------------
public class Animation {
    private final BufferedImage[] frames;
    private final double frameDurationMs;
    private final boolean loop;

    private int currentFrame = 0;
    private double accumulatorMs = 0.0;
    private boolean finished = false;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public Animation(BufferedImage[] frames, double frameDurationMs, boolean loop){
        this.frames = frames;
        this.frameDurationMs = frameDurationMs;
        this.loop = loop;
    }
    //-------------------------------------------------------------

    /**
     * Update the animation: increments counter and advances to the next frame if necessary
     */
    //-------------------------------------------------------------
    public void update(double deltaMs){
        if (finished || frames.length <= 1) {
            return;
        }

        accumulatorMs += deltaMs;
        while (accumulatorMs >= frameDurationMs) {
            accumulatorMs -= frameDurationMs;
            currentFrame++;
            if (currentFrame >= frames.length) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.length - 1;
                    finished = true;
                    break;
                }
            }
        }
    }
    //-------------------------------------------------------------

    /**
     * Reset the animation to the first frame
     */
    //-------------------------------------------------------------
    public void reset() {
        currentFrame = 0;
        accumulatorMs = 0.0;
        finished = false;
    }
    //-------------------------------------------------------------

    //GETTERS
    //-------------------------------------------------------------
    public BufferedImage getCurrentFrame(){
        return frames[currentFrame];
    }
    //-------------------------------------------------------------

    public boolean isFinished(){
        return finished;
    }
    //--------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
