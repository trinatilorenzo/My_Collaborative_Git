package view.Animation;

import java.util.HashMap;
import java.util.Map;

/**
 * ANIMATION MANAGER CLASS is responsible for managing multiple animations for an entity.
 * It provides functionality to add, switch, and update animations, as well as retrieve the currently active animation.
 */

//-------------------------------------------------------------------------------------------------------------------
public class AnimationManager {

    private final Map<String, Animation> animations = new HashMap<>();
    private Animation currentAnimation;

    //-------------------------------------------------------------
    public void addAnimation(String name, Animation animation){
        animations.put(name, animation);
        if (currentAnimation == null){
            currentAnimation = animation;
        }
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void playAnimation(String name){
        Animation animation = animations.get(name);
        if (animation != null && animation != currentAnimation){
            animation.reset();
            currentAnimation = animation;
        }
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void update(double deltaMs){
        if (currentAnimation != null){
            currentAnimation.update(deltaMs);
        }
    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public Animation getCurrent() {
        if (currentAnimation != null) {
            return currentAnimation;
        }
        return null;
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------
