package view.renderer.entity;

import main.CONFIG.EntityConfig;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.TorchState;
import model.entity.EnemyTorch;
import view.SpriteLoader;
import view.Animation.Animation;
import view.Animation.AnimationManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The TorchRenderer class is responsible for rendering the Torch Enemy entity,
 * managing its animations, drawing the dynamic health bar, and rendering debug hitboxes.
 */
//-------------------------------------------------------------------------------------------------------------------
public class TorchRenderer {

    private final EntityConfig entityConfig;
    // ConcurrentHashMaps to handle multiple instances of EnemyTorch safely
    private final ConcurrentHashMap<EnemyTorch, AnimationManager> managerByEnemy;
    private final ConcurrentHashMap<EnemyTorch, TorchState> previousStateByEnemy;

    private BufferedImage[] idleFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] attackRightFrames;
    private BufferedImage[] attackDownFrames;
    private BufferedImage[] attackUpFrames;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public TorchRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        this.managerByEnemy = new ConcurrentHashMap<>();
        this.previousStateByEnemy = new ConcurrentHashMap<>();
        loadAnimations();
    }

    //-------------------------------------------------------------
    private void loadAnimations() {
        // Loads the sprite sheet dedicated to the Torch enemy
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Torch/Torch_Yellow.png");

        // Extracts animation frames using the same row structure as the Player
        idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 7, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        attackRightFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        attackDownFrames = SpriteLoader.getAnimationFrames(sheetImage, 3, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        attackUpFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
    }

    /**
     * An animation Manger for each Torch entity
     */
    //--------------------------------------------------------------
    private AnimationManager getManager(EnemyTorch torch) {
        return managerByEnemy.computeIfAbsent(torch, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("idle", new Animation(idleFrames, 120, true));
            manager.addAnimation("walk", new Animation(walkFrames, 90, true));
            manager.addAnimation("attack_right", new Animation(attackRightFrames, 60, false));
            manager.addAnimation("attack_down", new Animation(attackDownFrames, 60, false));
            manager.addAnimation("attack_up", new Animation(attackUpFrames, 60, false));
            manager.playAnimation("idle");
            return manager;
        });
    }

    /**
     * Change the TNT animation based on his state
     */
    //-------------------------------------------------------------
    public void update(EnemyTorch torch, double deltaMs) {
        AnimationManager manager = getManager(torch);

        TorchState currentState = torch.getState();
        TorchState previousState = previousStateByEnemy.getOrDefault(torch, TorchState.APPROACH);

        boolean attackJustStarted = (currentState == TorchState.ATTACK_COMBO || currentState == TorchState.DASH) 
                                    && (previousState != TorchState.ATTACK_COMBO && previousState != TorchState.DASH);

        // Map logical AI duel states to visual animations
        switch (currentState) {
            case GUARD, RECOVERY -> {
                // Uses idle frames during guard/recovery states (fatigue or blocking stance)
                manager.playAnimation("idle"); 
            }
            case APPROACH -> manager.playAnimation("walk");
            
            case ATTACK_COMBO, DASH -> {
                // Directional attack rendering based on where the enemy is facing
                if (torch.getDirection() == Direction.DOWN) {
                    manager.playAnimation("attack_down");
                } else if (torch.getDirection() == Direction.UP) {
                    manager.playAnimation("attack_up");
                } else {
                    manager.playAnimation("attack_right");
                }

                if (attackJustStarted) {
                    manager.getCurrent().reset();
                }

                // Notify the model when the attack animation finishes to cycle back states
                if (manager.getCurrent().isFinished()) {
                    torch.completeAttackAnimation();
                }
            }
            case DEAD -> {
                removeEnemy(torch);
            }
        }

        manager.update(deltaMs);
        previousStateByEnemy.put(torch, currentState);
    }

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, EnemyTorch torch, int screenX, int screenY) {
        AnimationManager manager = getManager(torch);
        BufferedImage frame = manager.getCurrent().getCurrentFrame();

        int width = entityConfig.TORCH_SPRITE_WIDTH * 2;
        int height = entityConfig.TORCH_SPRITE_HEIGHT * 2;
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        // Flip image horizontally if facing left (assuming base sprite faces right)
        if (!torch.isFacingRight()) {
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }


        // DYNAMIC HEALTH BAR (Appears only after taking the first hit)
        if (torch.getLife() < torch.getMaxLife() && !torch.isDead()) {
            int barWidth = 100; 
            int barHeight = 6;
            int barX = screenX - barWidth / 2;
            int barY = screenY - barHeight - 100; // Positions bar safely above the head

            double lifePercent = (double) torch.getLife() / torch.getMaxLife();
            if (lifePercent < 0) lifePercent = 0;

            int currentBarWidth = (int) (barWidth * lifePercent);
            int roundness = 5;
            // Background (Black outline box)
            g2.setColor(Color.gray);
            g2.fillRoundRect(barX, barY, barWidth, barHeight, roundness, roundness);
            
            // Health fill (Fiery Orange/Red color)
            g2.setColor(new Color(255, 69, 0)); 
            g2.fillRoundRect(barX, barY, currentBarWidth, barHeight, roundness, roundness);
            
            // Border trim
            g2.setColor(new Color(30, 30, 30));
            g2.drawRoundRect(barX, barY, barWidth, barHeight, roundness, roundness);
        }
    }
    //-------------------------------------------------------------

    /**
     * Debug method to draw the tnt's solid area and interaction radius.
     */
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, EnemyTorch torch, int screenX, int screenY) {
        // 1. Body Hitbox (Solid Area)
        Rectangle solid = torch.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80)); // Semi-transparent Red
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        // 2. Melee Attack Range (Combo Trigger Radius)
        g2.setColor(new Color(255, 127, 0, 60)); // Semi-transparent Orange
        int attackRadius = entityConfig.TORCH_MELEE_RANGE; 
        g2.fillOval(screenX - attackRadius, screenY - attackRadius, 2 * attackRadius, 2 * attackRadius);
        g2.setColor(Color.ORANGE);
        g2.drawOval(screenX - attackRadius, screenY - attackRadius, 2 * attackRadius, 2 * attackRadius);

        // 3. Player Engagement Range (Detection/Chasing Radius)
        g2.setColor(new Color(0, 150, 255, 40)); // Semi-transparent Blue
        int detectRadius = entityConfig.TORCH_DASH_RANGE_TRIGGER; 
        g2.fillOval(screenX - detectRadius, screenY - detectRadius, 2 * detectRadius, 2 * detectRadius);
        g2.setColor(Color.BLUE);
        g2.drawOval(screenX - detectRadius, screenY - detectRadius, 2 * detectRadius, 2 * detectRadius);


    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void removeEnemy(EnemyTorch torch) {
        managerByEnemy.remove(torch);
        previousStateByEnemy.remove(torch);
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------