package com.touhoumaid.eatproperly.trinket;

/**
 * Edge-triggered clear for trinket-managed potion effects (night vision, speed, …).
 * Clearing every unequipped tick would strip potion / other-mod sources of the same effect.
 */
public final class TrinketManagedEffectPolicy {
    private TrinketManagedEffectPolicy() {
    }

    /**
     * @param equippedNow   trinket currently equipped
     * @param wasProviding  this mod applied/refreshed the effect on a prior loaded tick
     */
    public static Tick decide(boolean equippedNow, boolean wasProviding) {
        if (equippedNow) {
            return new Tick(true, false, true);
        }
        if (wasProviding) {
            return new Tick(false, true, false);
        }
        return new Tick(false, false, false);
    }

    public record Tick(boolean provide, boolean clear, boolean providingAfter) {
    }
}
