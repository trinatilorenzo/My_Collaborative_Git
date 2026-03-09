package view.Animation;

import java.awt.image.BufferedImage;

/**
 * ANIMATION CLASS
 * represent an animation made by multiple frames from a spritesheet
 */

//-------------------------------------------------------------------------------------------------------------------
public class Animation {
    private final BufferedImage[] frames;
    private final int frameDelay; // number of time in game frames to wait before switching to the next animation frame
    private int currentFrame = 0; // index of the current animation frame
    private int frameCounter = 0; // counts how many game frames have passed since the last animation frame change
    private boolean loop = true; //to distinguish between repeated animations and one-time animations
    private boolean finished = false; // to check if a one-time animation has finished

    // COSTRUCTOR
    //-------------------------------------------------------------
    public Animation(BufferedImage[] frames, int frameDelay, boolean loop){
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.loop = loop;
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void update(){
        if (finished) return;// if the animation is finished and it's a one-time animation, do not update anymore
        
        frameCounter++;
        if (frameCounter >= frameDelay) {
            currentFrame++;
            frameCounter = 0;
            if (currentFrame >= frames.length) { // reached the end of the animation
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frames.length - 1; // stay on the last frame
                    finished = true;
                }
            }
        }
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public BufferedImage getCurrentFrame(){
        return frames[currentFrame];
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public boolean isFinished(){
        return finished;
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void reset(){
        currentFrame = 0;
        frameCounter = 0;
        finished = false;
    }
    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------