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
 * TorchRenderer gestisce il rendering visivo e le animazioni dell'Enemy Torch,
 * inclusa la barra della salute dinamica sopra la testa.
 */
public class TorchRenderer {

    private final EntityConfig entityConfig;
    // Mappa per tenere traccia dei manager delle animazioni per ogni istanza di EnemyTorch (in caso ce ne fossero più di una)
    private final ConcurrentHashMap<EnemyTorch, AnimationManager> managerByEnemy;
    private final ConcurrentHashMap<EnemyTorch, TorchState> previousStateByEnemy;

    private BufferedImage[] idleFrames;
    private BufferedImage[] walkFrames;
    private BufferedImage[] attackRightFrames;
    private BufferedImage[] attackDownFrames;
    private BufferedImage[] attackUpFrames;

    // COSTRUTTORE
    //-------------------------------------------------------------
    public TorchRenderer(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        this.managerByEnemy = new ConcurrentHashMap<>();
        this.previousStateByEnemy = new ConcurrentHashMap<>();
        loadAnimations();
    }

    //-------------------------------------------------------------
    private void loadAnimations() {
        // Carica la sprite sheet dedicata a Torch (Assicurati che il percorso sia corretto)
        BufferedImage sheetImage = SpriteLoader.loadSpriteSheet("/res/npc/Enemy_Torch/Torch_Yellow.png");

        // Estrae i frame usando le stesse righe della struttura del Player
        idleFrames = SpriteLoader.getAnimationFrames(sheetImage, 0, 1, 7, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        walkFrames = SpriteLoader.getAnimationFrames(sheetImage, 1, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        attackRightFrames = SpriteLoader.getAnimationFrames(sheetImage, 2, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        attackDownFrames = SpriteLoader.getAnimationFrames(sheetImage, 3, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
        attackUpFrames = SpriteLoader.getAnimationFrames(sheetImage, 4, 1, 6, entityConfig.TORCH_SPRITE_WIDTH, entityConfig.TORCH_SPRITE_HEIGHT);
    }

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

    //-------------------------------------------------------------
    public void update(EnemyTorch torch, double deltaMs) {
        AnimationManager manager = getManager(torch);
        TorchState currentState = torch.getState();
        TorchState previousState = previousStateByEnemy.getOrDefault(torch, TorchState.APPROACH);

        boolean attackJustStarted = (currentState == TorchState.ATTACK_COMBO || currentState == TorchState.DASH) 
                                    && (previousState != TorchState.ATTACK_COMBO && previousState != TorchState.DASH);

        // Mappatura degli stati logici del duello sulle animazioni grafiche
        switch (currentState) {
            case GUARD -> {
                // Durante la parata usa il primo frame dell'attacco o l'idle a seconda della sprite sheet
                manager.playAnimation("idle"); 
            }
            case RECOVERY -> {
                // Affaticato: usa l'idle (o un'animazione di stanchezza se presente)
                manager.playAnimation("idle");
            }
            case APPROACH -> manager.playAnimation("walk");
            
            case ATTACK_COMBO, DASH -> {
                // Direziona l'attacco in base a dove sta guardando il mostro
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

                // Quando l'animazione di attacco finisce, comunica al modello di cambiare stato
                if (manager.getCurrent().isFinished()) {
                    torch.completeAttackAnimation();
                }
            }
            case DEAD -> {
                // Se hai dei deathFrames inseriscili qui, altrimenti si ferma sull'ultimo frame dell'attacco/idle
            }
        }

        manager.update(deltaMs);
        previousStateByEnemy.put(torch, currentState);
    }

    //-------------------------------------------------------------
    public void draw(Graphics2D g2, EnemyTorch torch, int screenX, int screenY) {
        AnimationManager manager = getManager(torch);
        BufferedImage frame = manager.getCurrent().getCurrentFrame();

        int width = entityConfig.TORCH_SPRITE_WIDTH;
        int height = entityConfig.TORCH_SPRITE_HEIGHT;
        int drawX = screenX - width / 2;
        int drawY = screenY - height / 2;

        // Specchia l'immagine orizzontalmente se guarda a sinistra (se la sprite base guarda a destra)
        if (!torch.isFacingRight()) {
            g2.drawImage(frame, drawX + width, drawY, -width, height, null);
        } else {
            g2.drawImage(frame, drawX, drawY, width, height, null);
        }

        // BARRA DELLA VITA DINAMICA (Appare solo dopo il primo colpo subito)
        if (torch.getLife() < torch.getMaxLife() && !torch.isDead()) {
            drawHealthBar(g2, torch, screenX, drawY);
        }
    }

    //-------------------------------------------------------------
    private void drawHealthBar(Graphics2D g2, EnemyTorch torch, int screenX, int enemyTopY) {
        int barWidth = 45; // Leggermente più grande per il mini-boss
        int barHeight = 6;
        int barX = screenX - barWidth / 2;
        int barY = enemyTopY - barHeight - 10; // 10 pixel sopra la testa

        double lifePercent = (double) torch.getLife() / torch.getMaxLife();
        if (lifePercent < 0) lifePercent = 0;

        int currentBarWidth = (int) (barWidth * lifePercent);

        // Sfondo nero
        g2.setColor(Color.BLACK);
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Barra della salute (Arancione/Rossa per un mostro di fuoco!)
        g2.setColor(new Color(255, 69, 0)); 
        g2.fillRect(barX, barY, currentBarWidth, barHeight);

        // Bordino di finitura
        g2.setColor(new Color(30, 30, 30));
        g2.drawRect(barX, barY, barWidth, barHeight);
    }

    //-------------------------------------------------------------
    public void removeEnemy(EnemyTorch torch) {
        managerByEnemy.remove(torch);
        previousStateByEnemy.remove(torch);
    }
}