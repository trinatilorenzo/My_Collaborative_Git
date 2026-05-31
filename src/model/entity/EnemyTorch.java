package model.entity;

import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

import main.CONFIG.EntityConfig;
import main.CONFIG.SpawnPoint;
import main.CONFIG.enu.Direction;
import main.CONFIG.enu.TorchState;

/**
 * Boss enemy for level 3. Chases the player and attacks with an expanding
 * ring of fire (vortex) spawned from its own position.
 *
 * State machine:
 *
 *   WANDER ──(player in attack range)──► CHARGE ──(windup done)──► VORTEX ──(ring fades)──► WANDER
 *     ▲                                     │
 *     │   hit during CHARGE                 │ any hit (life > 0)
 *     └─────────────────────────────────────┘
 *
 * Hit feedback (flash, knockback) is handled entirely by the renderer —
 * the model has no HIT or STUNNED state.
 *
 * Movement speed, jitter, and charge duration scale with damage taken (see computePhase()).
 */
public class EnemyTorch extends Entity {

    private TorchState state;

    // Facing direction used by the renderer to pick the correct sprite row.
    private Direction facingDirection;

    // State timers (ms)
    private double chargeTimer;

    // Wandering / chasing AI
    private final Random random;
    private double dirX;
    private double dirY;
    private double moveTimer;   // when to pick a new wander direction
    private double jitterTimer; // when to apply the next chase jitter offset
    private double jitterAngle; // current angular offset applied to the chase direction

    // Shared list owned by the game world; the active vortex is added here on attack.
    private final List<TorchVortex> globalVortices;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public EnemyTorch(SpawnPoint spawnPoint, EntityConfig entityConfig, List<TorchVortex> globalVortices) {
        super(entityConfig);
        this.random         = new Random();
        this.globalVortices = globalVortices;
        initializeDefaultValues(spawnPoint);
    }

    private void initializeDefaultValues(SpawnPoint spawnPoint) {
        this.state        = TorchState.WANDER;
        this.life         = EntityConfig.TORCH_MAX_LIFE;
        this.maxLife      = EntityConfig.TORCH_MAX_LIFE;

        this.worldX       = spawnPoint.x();
        this.worldY       = spawnPoint.y();
        this.currentLayer = spawnPoint.layer();

        solidArea = new Rectangle(0, 0, EntityConfig.TORCH_HITBOX_WIDTH, EntityConfig.TORCH_HITBOX_HEIGHT);

        this.facingDirection = Direction.DOWN;
        this.dirX            = 0;
        this.dirY            = 1;

        chargeTimer = 0;
        moveTimer   = 0;
        jitterTimer = 0;
        jitterAngle = 0;
    }

    // -------------------------------------------------------------------------
    // Per-frame update
    // -------------------------------------------------------------------------

    /**
     * Updates the boss state machine and movement each frame.
     *
     * @param player  the player entity
     * @param deltaMs elapsed time in milliseconds since the last frame
     */
    public void update(Player player, double deltaMs) {
        super.update(); // Reset dx, dy, collision flags

        switch (state) {

            case WANDER:
                wander(player, deltaMs);
                facePlayer(player);
                if (isPlayerInRange(player, EntityConfig.TORCH_ATTACK_RADIUS)) {
                    enterCharge();
                }
                break;

            case CHARGE:
                // Boss is stationary during windup — the renderer plays the torch-raise
                // animation. This is the player's visual cue to move away.
                facePlayer(player);
                chargeTimer += deltaMs;
                if (chargeTimer >= getChargeDuration()) {
                    releaseVortex();
                }
                break;

            case VORTEX:
                // Boss stays still while the ring expands. The vortex is updated
                // independently by the game world each frame.
                // Transition back to WANDER once the ring has fully expanded.
                TorchVortex active = getActiveVortex();
                if (active != null && active.isFinished()) {
                    state = TorchState.WANDER;
                }
                break;

            case DEAD:
                break;
        }
    }

    // -------------------------------------------------------------------------
    // AI — movement
    // -------------------------------------------------------------------------

    /**
     * Wanders the room while the player is outside the attack radius.
     * Moves toward the player like EnemyDynamite, but with angular jitter
     * that increases with each phase to feel more erratic and threatening.
     */
    private void wander(Player player, double deltaMs) {
        double dxPlayer = player.getWorldX() - worldX;
        double dyPlayer = player.getWorldY() - worldY;
        double distance = Math.sqrt(dxPlayer * dxPlayer + dyPlayer * dyPlayer);

        if (distance > 0) {
            jitterTimer += deltaMs;
            if (jitterTimer >= EntityConfig.TORCH_JITTER_INTERVAL_MS) {
                jitterAngle = (random.nextDouble() - 0.5) * 2.0 * getMaxJitter();
                jitterTimer = 0;
            }

            double base    = Math.atan2(dyPlayer, dxPlayer);
            double jittered = base + jitterAngle;
            dirX = Math.cos(jittered);
            dirY = Math.sin(jittered);
        }

        double dist = getScaledSpeed() * (deltaMs / 1000.0);
        dx = (int) Math.round(dirX * dist);
        dy = (int) Math.round(dirY * dist);
    }

    /**
     * Updates facingDirection toward the player using the dominant axis.
     * Used by the renderer to pick the correct sprite row.
     */
    private void facePlayer(Player player) {
        int dxPlayer = player.getWorldX() - worldX;
        int dyPlayer = player.getWorldY() - worldY;

        if (Math.abs(dxPlayer) >= Math.abs(dyPlayer)) {
            facingDirection = dxPlayer >= 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            facingDirection = dyPlayer >= 0 ? Direction.DOWN : Direction.UP;
        }
    }

    // -------------------------------------------------------------------------
    // AI — attack
    // -------------------------------------------------------------------------

    /** Transitions to CHARGE and resets the windup timer. */
    private void enterCharge() {
        state       = TorchState.CHARGE;
        chargeTimer = 0;
    }

    /**
     * Spawns a vortex centered on the boss and transitions to VORTEX.
     * The boss stays still until the ring finishes expanding.
     */
    private void releaseVortex() {
        globalVortices.add(new TorchVortex(worldX, worldY, currentLayer));
        state = TorchState.VORTEX;
    }

    /**
     * Returns the last vortex in the global list, assumed to belong to this boss.
     * Returns null if the list is empty.
     *
     * NOTE: works correctly only when a single EnemyTorch is active at a time.
     * If multiple bosses are needed, store a direct reference in releaseVortex() instead.
     */
    private TorchVortex getActiveVortex() {
        if (globalVortices.isEmpty()) return null;
        return globalVortices.get(globalVortices.size() - 1);
    }

    // -------------------------------------------------------------------------
    // Damage
    // -------------------------------------------------------------------------

    /**
     * Applies one point of damage.
     *
     * Hitting the boss during CHARGE cancels the attack — the boss returns to
     * WANDER without firing. This rewards the player for attacking during the
     * windup window instead of running away.
     *
     * Hit flash and knockback are handled by the renderer, not the model.
     * An already-dead boss is immune.
     */
    public void takeDamage() {
        if (state == TorchState.DEAD) return;

        life--;

        if (life <= 0) {
            life  = 0;
            state = TorchState.DEAD;
            return;
        }

        // Cancel the attack if the player is fast enough to hit during windup.
        if (state == TorchState.CHARGE) {
            state = TorchState.WANDER;
        }
    }

    // -------------------------------------------------------------------------
    // Phase scaling
    // -------------------------------------------------------------------------

    /**
     * Returns the current combat phase based on remaining life fraction.
     *   Phase 1: 100% – 67%  (normal speed, low jitter, 900ms charge)
     *   Phase 2:  66% – 34%  (+25% speed, medium jitter, 650ms charge)
     *   Phase 3:  33% –  0%  (+40% speed, high jitter, 450ms charge)
     */
    private int computePhase() {
        double ratio = (double) life / maxLife;
        if (ratio > 0.66) return 1;
        if (ratio > 0.33) return 2;
        return 3;
    }

    /** Movement speed in px/s, scaled by phase. */
    private int getScaledSpeed() {
        return switch (computePhase()) {
            case 2  -> (int) (EntityConfig.TORCH_BASE_SPEED * 1.25);
            case 3  -> (int) (EntityConfig.TORCH_BASE_SPEED * 1.40);
            default -> EntityConfig.TORCH_BASE_SPEED;
        };
    }

    /**
     * Windup duration in ms, scaled by phase.
     * Shorter windups in later phases give the player less time to react.
     */
    private double getChargeDuration() {
        return switch (computePhase()) {
            case 2  -> EntityConfig.TORCH_CHARGE_DURATION_MS * 0.72;
            case 3  -> EntityConfig.TORCH_CHARGE_DURATION_MS * 0.50;
            default -> EntityConfig.TORCH_CHARGE_DURATION_MS;
        };
    }

    /**
     * Maximum angular jitter in radians, scaled by phase.
     * Higher values make the chase path harder to predict and dodge.
     */
    private double getMaxJitter() {
        return switch (computePhase()) {
            case 2  -> EntityConfig.TORCH_JITTER_MAX_RADIANS * 2.0;
            case 3  -> EntityConfig.TORCH_JITTER_MAX_RADIANS * 3.5;
            default -> EntityConfig.TORCH_JITTER_MAX_RADIANS;
        };
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Returns true if the player is within the given radius. Uses squared distance. */
    private boolean isPlayerInRange(Player player, double radius) {
        double dxPlayer = player.getWorldX() - worldX;
        double dyPlayer = player.getWorldY() - worldY;
        return (dxPlayer * dxPlayer + dyPlayer * dyPlayer) < radius * radius;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public TorchState getState()          { return state; }
    public Direction  getFacingDirection() { return facingDirection; }
    public boolean    isDead()             { return state == TorchState.DEAD; }

    /**
     * Normalised charge progress in [0.0, 1.0].
     * Used by the renderer to drive the torch-raise animation frame selection.
     */
    public double getChargeProgress() {
        if (state != TorchState.CHARGE) return 0;
        return Math.min(chargeTimer / getChargeDuration(), 1.0);
    }

    /** Current combat phase (1–3). Exposed for renderer visual cues if needed. */
    public int getPhase() { return computePhase(); }
}