package tinyswordsisland.view.renderer.entity;

import tinyswordsisland.config.EntityConfig;
import tinyswordsisland.model.enu.Direction;
import tinyswordsisland.model.enu.TorchState;
import tinyswordsisland.model.IRenderable;
import tinyswordsisland.view.SpriteLoader;
import tinyswordsisland.view.animation.Animation;
import tinyswordsisland.view.animation.AnimationManager;

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
    private final ConcurrentHashMap<IRenderable, AnimationManager> managerByEnemy;
    private final ConcurrentHashMap<IRenderable, TorchState> previousStateByEnemy;
    private final ConcurrentHashMap<IRenderable, AnimationManager> fireManagerByEnemy;

    private BufferedImage[] idleFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] attackRightFrames;
    private BufferedImage[] attackDownFrames;
    private BufferedImage[] attackUpFrames;

    private BufferedImage[] fireFrames;

    /**
     * CONSTRUCTOR
     */
    //-------------------------------------------------------------
    public TorchRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        this.managerByEnemy = new ConcurrentHashMap<>();
        this.previousStateByEnemy = new ConcurrentHashMap<>();
        this.fireManagerByEnemy = new ConcurrentHashMap<>();
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

        // Loads the sprite sheet dedicated to the fire
        BufferedImage fireSheet = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Torch/Fire.png");
        fireFrames = SpriteLoader.getAnimationFrames(fireSheet, 0, 1, 7, entityConfig.FIRE_SPRITE_WIDTH, entityConfig.FIRE_SPRITE_HEIGHT);
    }

    /**
     * An animation Manger for each Torch entity
     */
    //--------------------------------------------------------------
    private AnimationManager getManager(IRenderable torch) {
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
     * An animation Manger for each Torch entity
     */
    //--------------------------------------------------------------
    private AnimationManager getFireManager(IRenderable torch) {
        return fireManagerByEnemy.computeIfAbsent(torch, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("fire_effect", new Animation(fireFrames, 120, false));
            manager.playAnimation("fire_effect");
            return manager;
        });
    }

    /**
     * Change the animation based on his state
     */
    //-------------------------------------------------------------
    public void update(IRenderable torch, double deltaMs) {
        AnimationManager manager = getManager(torch);
        AnimationManager fireManager = getFireManager(torch);

        TorchState currentState = TorchState.values()[torch.getRenderState()];
        TorchState previousState = previousStateByEnemy.getOrDefault(torch, TorchState.APPROACH);

        boolean attackJustStarted = (currentState == TorchState.ATTACK_COMBO && previousState != TorchState.ATTACK_COMBO);

        // Map logical AI duel states to visual animations
        switch (currentState) {
            case RECOVERY -> {
                // Uses idle frames during guard/recovery states (fatigue or blocking stance)
                manager.playAnimation("idle");
            }
            case APPROACH -> manager.playAnimation("walk");

            case ATTACK_COMBO-> {
                // Directional attack rendering based on where the enemy is facing
                Direction direction = Direction.values()[torch.getRenderDirection()];
                if (direction == Direction.DOWN) {
                    manager.playAnimation("attack_down");
                } else if (direction == Direction.UP) {
                    manager.playAnimation("attack_up");
                } else {
                    manager.playAnimation("attack_right");
                }

                if (attackJustStarted) {
                    manager.getCurrent().reset();
                    fireManager.getCurrent().reset();
                }

                fireManager.update(deltaMs);

                // State transitions are managed in model update logic.
            }
            case DEAD -> {
                removeEnemy(torch);
            }
        }

        manager.update(deltaMs);
        previousStateByEnemy.put(torch, currentState);
    }

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, IRenderable torch, int screenX, int screenY) {
        AnimationManager manager = getManager(torch);
        BufferedImage frame = manager.getCurrent().getCurrentFrame();

        int width = (int) (entityConfig.TORCH_SPRITE_WIDTH * entityConfig.TORCH_SCALE);
        int height = (int) (entityConfig.TORCH_SPRITE_HEIGHT * entityConfig.TORCH_SCALE);
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        // Effect of recovery
        Composite originalComposite = g2.getComposite();
        TorchState currentState = TorchState.values()[torch.getRenderState()];
        if (currentState == TorchState.RECOVERY) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        // Flip image horizontally if facing left (assuming base sprite faces right)
        if (!torch.isFacingRightRender()) {
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }

        g2.setComposite(originalComposite);

        // Draw the fire
        if (currentState == TorchState.ATTACK_COMBO) {

            BufferedImage fireFrame = getFireManager(torch).getCurrent().getCurrentFrame();

            // Calculate position of the fire
            int fireCenterX = screenX;
            int fireCenterY = screenY;
            int offset = (int) width / 3;

            switch (Direction.values()[torch.getRenderDirection()]) {
                case RIGHT -> fireCenterX += offset;
                case LEFT  -> fireCenterX -= offset;
                case DOWN  -> fireCenterY += offset;
                case UP    -> fireCenterY -= offset;
            }
            int fireX = fireCenterX - EntityConfig.FIRE_SPRITE_WIDTH / 2;
            int fireY = fireCenterY - EntityConfig.FIRE_SPRITE_HEIGHT / 2;

            g2.drawImage(fireFrame, fireX, fireY, EntityConfig.FIRE_SPRITE_WIDTH, EntityConfig.FIRE_SPRITE_HEIGHT, null);

        }


        // DYNAMIC HEALTH BAR (Appears only after taking the first hit)
        if (torch.getLifeRender() < torch.getMaxLifeRender() && !torch.isDeadRender()) {
            int barWidth = 100;
            int barHeight = 6;
            int barX = screenX - barWidth / 2;
            int barY = screenY - barHeight - 100; // Positions bar safely above the head

            double lifePercent = (double) torch.getLifeRender() / torch.getMaxLifeRender();
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
    public void drawSolidArea(Graphics2D g2, IRenderable torch, int screenX, int screenY) {
        // 1. Body Hitbox (Solid Area)
        Rectangle solid = torch.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80)); // Semi-transparent Red
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);


        if (TorchState.values()[torch.getRenderState()] == TorchState.ATTACK_COMBO) {
            int attackDrawX = torch.getAttackAreaX() - torch.getWorldX() + screenX;
            int attackDrawY = torch.getAttackAreaY() - torch.getWorldY() + screenY;

            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(attackDrawX, attackDrawY, torch.getAttackAreaWidth(), torch.getAttackAreaHeight());
            g2.setColor(Color.RED);
            g2.drawRect(attackDrawX, attackDrawY, torch.getAttackAreaWidth(), torch.getAttackAreaHeight());
        }

    }
    //-------------------------------------------------------------

    //-------------------------------------------------------------
    public void removeEnemy(IRenderable torch) {
        managerByEnemy.remove(torch);
        fireManagerByEnemy.remove(torch);
        previousStateByEnemy.remove(torch);
    }
    //-------------------------------------------------------------

}
//-------------------------------------------------------------------------------------------------------------------