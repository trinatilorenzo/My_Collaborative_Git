package view.renderer.entity;

import main.CONFIG.EntityConfig;
import model.entity.DynamiteProjectile;
import model.entity.EnemyDynamite;
import view.Animation.Animation;
import view.Animation.AnimationManager;
import view.SpriteLoader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

public class DynamiteRender {
    private final ConcurrentHashMap<EnemyDynamite, AnimationManager> managerByEnemy;
    private final ConcurrentHashMap<EnemyDynamite, Integer> lastAttackCountByEnemy;
    private final ConcurrentHashMap<EnemyDynamite, Boolean> attackAnimationActiveByEnemy;
    private final EntityConfig entityConfig;

    private BufferedImage[] wanderFrames;
    private BufferedImage[] attackFrames;

    private BufferedImage[] projectileFrames;

    // COSTRUCTOR
    //-------------------------------------------------------------
    public DynamiteRender(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        loadAnimations();
        managerByEnemy = new ConcurrentHashMap<>();
        lastAttackCountByEnemy = new ConcurrentHashMap<>();
        attackAnimationActiveByEnemy = new ConcurrentHashMap<>();
    }

    //-------------------------------------------------------------
    private void loadAnimations() {
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Dynamite/TNT_Red.png");
        BufferedImage projectileImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Dynamite/Dynamite.png");
        wanderFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
        attackFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, entityConfig.DYNAMITE_SPRITE_WIDTH, entityConfig.DYNAMITE_SPRITE_HEIGHT);
        
        projectileFrames = SpriteLoader.getAnimationFrames(projectileImage, 0, 1, 6, entityConfig.PROJECTILE_SPRITE_WIDTH, entityConfig.PROJECTILE_SPRITE_HEIGHT);
    }

    //--------------------------------------------------------------
    private AnimationManager getManager(EnemyDynamite dynamite) {
        return managerByEnemy.computeIfAbsent(dynamite, k -> {
            AnimationManager manager = new AnimationManager();
            manager.addAnimation("wander", new Animation(wanderFrames, 90, true));
            manager.addAnimation("attack", new Animation(attackFrames, 100, false));
            manager.playAnimation("wander"); // Stato iniziale
            return manager;
        });
    }

    //-------------------------------------------------------------
    public void update(EnemyDynamite enemy, double deltaMs) {
        AnimationManager manager = getManager(enemy);

        int previousAttackCount = lastAttackCountByEnemy.getOrDefault(enemy, 0);
        if (enemy.getAttackCount() != previousAttackCount) {
            manager.playAnimation("attack");
            lastAttackCountByEnemy.put(enemy, enemy.getAttackCount());
            attackAnimationActiveByEnemy.put(enemy, true);
        }

        manager.update(deltaMs);

        if (Boolean.TRUE.equals(attackAnimationActiveByEnemy.get(enemy)) && manager.getCurrent().isFinished()) {
            manager.playAnimation("wander");
            attackAnimationActiveByEnemy.put(enemy, false);
        }
    }

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, EnemyDynamite enemy, int screenX, int screenY) {
        AnimationManager manager = getManager(enemy);
        BufferedImage frame = manager.getCurrent().getCurrentFrame();
        
        int width = EntityConfig.DYNAMITE_SPRITE_WIDTH;
        int height = entityConfig.DYNAMITE_SPRITE_HEIGHT;
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        if (!enemy.isFacingRight()){
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }

        // Draw life bar
        if (enemy.getLife() < enemy.getMaxLife()) {
            int barWidth = width / 2; 
            int barHeight = 6;
            int barX = screenX - barWidth / 2;
            int barY = drawY - barHeight - 5;
            double healthPercent = (double) enemy.getLife() / enemy.getMaxLife();
            if (healthPercent < 0) healthPercent = 0;
            int currentBarWidth = (int) (barWidth * healthPercent);
            g2.setColor(Color.BLACK);
            g2.fillRect(barX, barY, barWidth, barHeight);
            g2.setColor(Color.RED);
            g2.fillRect(barX, barY, currentBarWidth, barHeight);  
            g2.setColor(new Color(50, 50, 50));
            g2.drawRect(barX, barY, barWidth, barHeight);

        }
    }

    //-------------------------------------------------------------
    public void removeEnemy(EnemyDynamite enemy) {
        managerByEnemy.remove(enemy);
        lastAttackCountByEnemy.remove(enemy);
        attackAnimationActiveByEnemy.remove(enemy);
    }

    //-------------------------------------------------------------
    public void drawProjectile(Graphics2D g2, DynamiteProjectile proj, int screenX, int screenY) {
        BufferedImage frame = projectileFrames[0]; // Espandibile con animazione in futuro

        int w = entityConfig.PROJECTILE_SPRITE_WIDTH;
        int h = entityConfig.PROJECTILE_SPRITE_HEIGHT;
        double angle = proj.getAngle();

        java.awt.geom.AffineTransform old = g2.getTransform();
        g2.translate(screenX, screenY);
        g2.rotate(angle);
        g2.drawImage(frame, -w / 2, -h / 2, w, h, null);
        g2.setTransform(old);
    }

    //DEBUG METOD
    //-------------------------------------------------------------
    public void drawSolidArea(Graphics2D g2, EnemyDynamite enemyDynamite, int screenX, int screenY) {
        Rectangle solid = enemyDynamite.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 0, 0, 80));
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.RED);
        g2.drawRect(drawX, drawY, solid.width, solid.height);

        // Attack Radius
        g2.setColor(new Color(93, 255, 0, 80));
        int r = entityConfig.DYNAMITE_ATTACKING_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.GREEN);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);

        // Detection Radius
        g2.setColor(new Color(0, 84, 255, 80));
        r = entityConfig.DYNAMITE_DETECTION_RADIUS;
        g2.fillOval(screenX - r, screenY - r, 2 * r, 2 * r);
        g2.setColor(Color.BLUE);
        g2.drawOval(screenX - r, screenY - r, 2 * r, 2 * r);
    }

    public void drawProjectileSolidArea(Graphics2D g2, DynamiteProjectile proj, int screenX, int screenY) {
        Rectangle solid = proj.getSolidArea();
        int drawX = screenX - solid.width / 2;
        int drawY = screenY - solid.height / 2;

        g2.setColor(new Color(255, 165, 0, 80)); 
        g2.fillRect(drawX, drawY, solid.width, solid.height);
        g2.setColor(Color.ORANGE);
        g2.drawRect(drawX, drawY, solid.width, solid.height);
    }
}

