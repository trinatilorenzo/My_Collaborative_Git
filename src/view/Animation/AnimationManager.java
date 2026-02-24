package view.Animation;

import java.util.HashMap;
import java.util.Map;

// - ANIMATION MANAGER CLASS
// manages multiple animations for an entity
//-------------------------------------------------------------------------------------------------------------------
public class AnimationManager {

    private final Map<String, Animation> animations = new HashMap<>();
    private Animation currentAnimation;

    public void addAnimation(String name, Animation animation){
        animations.put(name, animation);
        if (currentAnimation == null){
            currentAnimation = animation;
        }

    }//end add method

    public void playAnimation(String name){
        Animation animation = animations.get(name);
        if (animation != null && animation != currentAnimation){
            animation.reset();
            currentAnimation = animation;
        }
    }//end play method

    public void update(){
        if (currentAnimation != null){
            currentAnimation.update();
        }
    }//end update method

    public Animation getCurrent(){
        if (currentAnimation != null){
            return currentAnimation;
        }
        return null;
    }//end getCurrentFrame method

}// end ANIMATION MANAGER CLASS
//-------------------------------------------------------------------------------------------------------------------
