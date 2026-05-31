package model.entity;

import main.CONFIG.EntityConfig;

/**
 * Expanding ring of fire spawned by EnemyTorch during its VORTEX state.
 *
 * The vortex is not a projectile — it has no direction. It grows outward from
 * its origin at a constant speed and deals damage to the player exactly once,
 * at the moment the expanding radius crosses the player's position.
 *
 * The renderer reads getRadius() each frame to draw the ring sprite at the
 * correct scale. Once isFinished() returns true the vortex can be discarded.
 */
public class TorchVortex {

    private final int originX;
    private final int originY;
    private final int layer;

    private double radius;
    private boolean hasDealtDamage;
    private boolean finished;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a vortex centered on the given world position.
     *
     * @param originX world X of the caster (center of the ring)
     * @param originY world Y of the caster (center of the ring)
     * @param layer   world layer the vortex belongs to
     */
    public TorchVortex(int originX, int originY, int layer) {
        this.originX       = originX;
        this.originY       = originY;
        this.layer         = layer;
        this.radius        = 0;
        this.hasDealtDamage = false;
        this.finished      = false;
    }

    // -------------------------------------------------------------------------
    // Per-frame update
    // -------------------------------------------------------------------------

    /**
     * Expands the ring and checks whether it has reached the player.
     *
     * Damage is applied exactly once: the frame the ring radius first exceeds
     * the player's distance from the origin. After reaching its maximum radius
     * the vortex marks itself as finished so the owner can discard it.
     *
     * @param player  the player to check against
     * @param deltaMs elapsed time in milliseconds since the last frame
     */
    public void update(Player player, double deltaMs) {
        if (finished) return;

        radius += EntityConfig.VORTEX_EXPAND_SPEED * (deltaMs / 1000.0);

        // Check if the ring has swept past the player this frame.
        if (!hasDealtDamage && player.getCurrentLayer() == layer) {
            double dx = player.getWorldX() - originX;
            double dy = player.getWorldY() - originY;
            double distanceSq = dx * dx + dy * dy;

            // The ring deals damage when its radius first crosses the player's position.
            // We compare squared values to avoid a sqrt per frame.
            double radiusSq = radius * radius;
            if (distanceSq <= radiusSq) {
                player.takeDamage();
                hasDealtDamage = true;
            }
        }

        if (radius >= EntityConfig.VORTEX_MAX_RADIUS) {
            finished = true;
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Current outer radius of the ring in pixels. Used by the renderer. */
    public double getRadius()  { return radius; }

    /** World X of the ring center. */
    public int getOriginX()    { return originX; }

    /** World Y of the ring center. */
    public int getOriginY()    { return originY; }

    /** World layer this vortex belongs to. */
    public int getLayer()      { return layer; }

    /**
     * Returns true when the ring has reached its maximum radius and can be
     * removed from the active vortex list.
     */
    public boolean isFinished() { return finished; }
}