package view.Animation;

/**
 * Time-based frame cursor shared by animations.
 * Keeps track of the current frame index using elapsed milliseconds.
 */
public class FrameTimeline {
    private final int frameCount;
    private final double frameDurationMs;
    private final boolean loop;

    private int currentFrame = 0;
    private double accumulatorMs = 0.0;
    private boolean finished = false;

    public FrameTimeline(int frameCount, double frameDurationMs, boolean loop) {
        this.frameCount = frameCount;
        this.frameDurationMs = frameDurationMs;
        this.loop = loop;
    }

    public void update(double deltaMs) {
        if (finished || frameCount <= 1) {
            return;
        }

        accumulatorMs += deltaMs;
        while (accumulatorMs >= frameDurationMs) {
            accumulatorMs -= frameDurationMs;
            currentFrame++;
            if (currentFrame >= frameCount) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frameCount - 1;
                    finished = true;
                    break;
                }
            }
        }
    }

    public void reset() {
        currentFrame = 0;
        accumulatorMs = 0.0;
        finished = false;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public boolean isFinished() {
        return finished;
    }
}
