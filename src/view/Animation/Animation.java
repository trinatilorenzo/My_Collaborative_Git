package view.Animation;

import java.awt.image.BufferedImage;

/**
 * ANIMATION CLASS
 * represent an animation made by multiple frames from a spritesheet
 */

//-------------------------------------------------------------------------------------------------------------------
public class Animation {
    private final BufferedImage[] frames;
    private final FrameTimeline timeline;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public Animation(BufferedImage[] frames, double frameDurationMs, boolean loop){
        this.frames = frames;
        this.timeline = new FrameTimeline(frames.length, frameDurationMs, loop);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void update(double deltaMs){
        timeline.update(deltaMs);
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public BufferedImage getCurrentFrame(){
        return frames[timeline.getCurrentFrame()];
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public boolean isFinished(){
        return timeline.isFinished();
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void reset(){
        timeline.reset();
    }
    //-------------------------------------------------------------
}
//-------------------------------------------------------------------------------------------------------------------
