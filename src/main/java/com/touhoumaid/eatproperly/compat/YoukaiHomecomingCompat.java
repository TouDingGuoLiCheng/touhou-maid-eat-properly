package com.touhoumaid.eatproperly.compat;

import dev.xkmc.youkaishomecoming.content.entity.youkai.YoukaiEntity;
import net.minecraft.world.entity.Entity;

/**
 * Soft-dependency bridge to Youkai's Homecoming (modid {@code youkaishomecoming}), used by the
 * lake fairy keepsake (§3.5). This class references {@code youkaishomecoming} classes directly, so
 * every entry point <b>must</b> be guarded by
 * {@code ModList.get().isLoaded(EatProperlyMod.YOUKAI_MOD_ID)} before calling in
 * (see {@code TrinketEffects}) — otherwise the class link will fail when the mod isn't installed.
 */
public final class YoukaiHomecomingCompat {
    private YoukaiHomecomingCompat() {
    }

    /**
     * True when {@code attacker} is a general Youkai's Homecoming youkai (fairies, tradeable
     * merchants like Rumia, boss youkai, etc.) whose hostility the keepsake should suppress.
     *
     * <p><b>TODO (deferred, per spec "API 不清楚就 stub"):</b> the mod's normal requirement that the
     * wearer be half-youkai/youkai/fairy-transformed before a youkai like Rumia will trade instead
     * of fight is enforced deep inside each youkai's own AI / interaction code
     * ({@code YoukaiEntity}, {@code IYoukaiMerchant}); no public capability/API to bypass just that
     * check was found while implementing this task. Only the "not attacked" half of the spec is
     * covered here; actually unlocking trade access without transforming is not implemented.
     * Revisit if Youkai's Homecoming exposes a transformation capability/API later.</p>
     */
    public static boolean isSuppressedAttacker(Entity attacker) {
        return attacker instanceof YoukaiEntity;
    }
}
