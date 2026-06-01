package model.entity;

import java.awt.Rectangle;
import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.TorchState;

public class EnemyTorch extends Entity {

    private TorchState state;
    private double stateTimer;
    private double attackCooldownMs;
    private Direction facingDirection;

    
    private int comboStep = 0;
    private boolean isBlocking = false;

    public EnemyTorch(SpawnPoint spawnPoint, EntityConfig entityConfig) {
        super(entityConfig);
        initializeDefaultValues(spawnPoint);
    }

    public void initializeDefaultValues(SpawnPoint spawnPoint) {
        this.state = TorchState.APPROACH;
        this.worldX = spawnPoint.x();
        this.worldY = spawnPoint.y();
        this.currentLayer = spawnPoint.layer();
        
        // Statistiche più alte del Dynamite per renderlo un mini-boss
        this.speed = EntityConfig.TORCH_START_SPEED; // Es: più veloce del Dynamite
        this.life = EntityConfig.TORCH_MAX_LIFE;    // Es: 10 o 15 vite invece di 3
        
        // Hitbox per le collisioni del corpo
        solidArea = new Rectangle(0, 0, EntityConfig.TORCH_HITBOX_WIDTH, EntityConfig.TORCH_HITBOX_HEIGHT);
    }

    public void update(Player player, double deltaMs) {
        super.update();

        if (attackCooldownMs > 0) attackCooldownMs -= deltaMs;
        stateTimer += deltaMs;

        // Faccia sempre il giocatore
        facePlayer(player);

        switch (state) {
            case APPROACH:
                double dist = getDistanceToPlayer(player);
                if (dist < EntityConfig.TORCH_MELEE_RANGE) {
                    // Troppo vicino! Passa alla guardia o attacca
                    state = Math.random() > 0.5 ? TorchState.GUARD : TorchState.ATTACK_COMBO;
                    stateTimer = 0;
                } else if (dist > EntityConfig.TORCH_DASH_RANGE_TRIGGER && attackCooldownMs <= 0) {
                    // Il player scappa? Carica il Dash!
                    state = TorchState.DASH;
                    stateTimer = 0;
                } else {
                    moveTowardsPlayer(player, deltaMs);
                }
                break;

            case GUARD:
                this.isBlocking = true;
                // Rimane fermo in guardia per 1.5 secondi, poi attacca o avanza
                if (stateTimer >= 1500) {
                    this.isBlocking = false;
                    state = TorchState.ATTACK_COMBO;
                    stateTimer = 0;
                }
                break;

            case ATTACK_COMBO:
                // Esegue una combo (gestita poi dal Renderer per i frame)
                executeComboLogic(player);
                break;

            case DASH:
                // Scatto fulmineo in avanti verso la posizione del player
                executeDashLogic(player, deltaMs);
                break;

            case RECOVERY:
                // Fermo e vulnerabile. Non fa nulla per 1 secondo.
                if (stateTimer >= 1000) {
                    state = TorchState.APPROACH;
                    attackCooldownMs = 2000; // Cooldown prima del prossimo attacco pesante
                    stateTimer = 0;
                }
                break;

            case DEAD:
                break;
        }
    }

    public void takeDamage() {
        if (state == TorchState.DEAD) return;

        // SE È IN GUARDIA, IL GIOCATORE VIENE RESPINTO (Knockback) O NON FA DANNO
        if (this.isBlocking) {
            // Qui potresti triggerare un evento audio di "Scudo/Parata" 
            System.out.println("Torch ha parato il colpo!");
            return; 
        }

        this.life--;
        if (this.life <= 0) {
            this.state = TorchState.DEAD;
        }
    }

    private double getDistanceToPlayer(Player player) {
        long dx = player.getWorldX() - worldX;
        long dy = player.getWorldY() - worldY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void moveTowardsPlayer(Player player, double deltaMs) {
        double dxPlayer = player.getWorldX() - worldX;
        double dyPlayer = player.getWorldY() - worldY;
        double distance = getDistanceToPlayer(player);

        if (distance > 0) {
            double dist = speed * (deltaMs / 1000.0);
            dx = (int) Math.round((dxPlayer / distance) * dist);
            dy = (int) Math.round((dyPlayer / distance) * dist);
        }
    }

    private void facePlayer(Player player) {
        this.facingDirection = (player.getWorldX() >= worldX) ? Direction.RIGHT : Direction.LEFT;
    }

    private void executeComboLogic(Player player) {
        // Logica per infliggere danno melee se il player è nel raggio d'azione
        // Finita la combo, passa in RECOVERY
        System.out.println("Torch attacca con la spada infuocata!");
        state = TorchState.RECOVERY;
        stateTimer = 0;
    }

    private void executeDashLogic(Player player, double deltaMs) {
        // Aumenta temporaneamente la velocità per fare uno scatto
        // Se colpisce il player fa danno, poi passa in RECOVERY
        if (stateTimer >= 400) { // Il dash dura 400ms
            state = TorchState.RECOVERY;
            stateTimer = 0;
        }
    }

    public void completeAttackAnimation() {
        // Questo metodo viene chiamato dal Renderer quando l'animazione di attacco finisce
        this.comboStep = 0; // Resetta la combo dopo l'attacco
        state = TorchState.RECOVERY; // Torna in recovery dopo l'attacco
    }

    // Getters per il Renderer
    public TorchState getState() { return state; }
    public boolean isFacingRight() { return facingDirection == Direction.RIGHT; }
    public boolean isDead() { return state == TorchState.DEAD; }

}