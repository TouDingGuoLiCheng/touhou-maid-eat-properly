package com.touhoumaid.eatproperly.trinket;

import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.compat.YoukaiHomecomingCompat;
import com.touhoumaid.eatproperly.item.ModItems;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import com.touhoumaid.eatproperly.sound.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Buff trinket effects. Who wears benefits: player 3-slot inventory → {@link Player};
 * maid TLM bauble slots → {@link EntityMaid}. Night sparrow is player-only.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class TrinketEffects {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String MOON_APPLE_PROGRESS = "MoonAppleProgress";
    private static final String MOON_APPLE_LAST_TICK = "MoonAppleLastTick";

    private static final int HEART_SEASONING_BONUS_PER_ITEM = 1;
    private static final double INSULATED_MEAL_BAG_DRAIN_REDUCTION = 0.4;
    private static final int SWEET_SOFT_CANDY_ABSORPTION_DURATION_TICKS = 100;

    /** Refresh only when effect is missing or about to expire; bar visibly counts down. */
    private static final int TRINKET_EFFECT_REFRESH_DURATION_TICKS = 6000;
    private static final int TRINKET_EFFECT_REFRESH_THRESHOLD_TICKS = 20;

    private static final double SCARLET_AMULET_MAX_HEALTH_PERCENT = 0.1;
    private static final UUID SCARLET_AMULET_MODIFIER_ID =
            UUID.fromString("b6e1f6d2-6f0a-4b8e-9a2f-1c9d7e6a3b40");

    private static final Map<UUID, Float> LAST_EXHAUSTION = new HashMap<>();
    /** Entities for whom this mod is currently providing night vision / speed via trinkets. */
    private static final Set<UUID> NIGHT_SPARROW_PROVIDING = new HashSet<>();
    private static final Set<UUID> SILVER_BELL_PROVIDING = new HashSet<>();
    /** Cooldown ticks remaining before the next walk chime may play. */
    private static final Map<UUID, Integer> SILVER_BELL_CHIME_COOLDOWN = new HashMap<>();
    private static final int SILVER_BELL_CHIME_INTERVAL_TICKS = 200; // ~10s at 20 TPS
    /** Min horizontal speed² this tick to count as walking (~0.03 blocks/tick). */
    private static final double SILVER_BELL_MOVE_THRESHOLD_SQ = 0.0009;

    private static boolean halvingInProgress = false;

    private TrinketEffects() {
    }

    /** Equipped trinkets for this living entity (empty if unsupported). */
    public static List<ItemStack> equippedOf(LivingEntity entity) {
        if (entity instanceof Player player) {
            return PlayerTrinketData.getEquipped(player);
        }
        if (entity instanceof EntityMaid maid) {
            return MaidTrinketData.getEquipped(maid);
        }
        return List.of();
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (event.getItem().getUseAnimation() != UseAnim.EAT) {
            return;
        }
        List<ItemStack> equipped = equippedOf(entity);
        if (equipped.isEmpty()) {
            return;
        }
        applyHeartSeasoning(entity, equipped);
        applySweetSoftCandy(entity, equipped);
    }

    private static void applyHeartSeasoning(LivingEntity entity, List<ItemStack> equipped) {
        int count = countEquipped(equipped, ModItems.HEART_SEASONING.get());
        int bonus = StackPolicy.ADDITIVE.mergeInt(count, HEART_SEASONING_BONUS_PER_ITEM);
        if (bonus <= 0) {
            return;
        }
        if (entity instanceof Player player) {
            player.getFoodData().eat(bonus, 0.0F);
        } else if (entity instanceof EntityMaid maid) {
            SatietyData.addSatiety(maid, bonus);
        }
    }

    private static void applySweetSoftCandy(LivingEntity entity, List<ItemStack> equipped) {
        if (!isEquipped(equipped, ModItems.SWEET_SOFT_CANDY.get())) {
            return;
        }
        entity.addEffect(new MobEffectInstance(
                MobEffects.ABSORPTION, SWEET_SOFT_CANDY_ABSORPTION_DURATION_TICKS, 0, false, true, true));
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof Player player) {
            List<ItemStack> equipped = PlayerTrinketData.getEquipped(player);
            tickExhaustionRefund(player, equipped);
            tickMoonAppleCandy(player, equipped);
            tickScarletAmulet(player, equipped);
            tickNightSparrowWrapper(player, equipped);
            tickSilverBellCharm(player, equipped);
            tickOnsenManjuBag(player, equipped);
            return;
        }
        if (entity instanceof EntityMaid maid) {
            List<ItemStack> equipped = MaidTrinketData.getEquipped(maid);
            tickMoonAppleCandyMaid(maid, equipped);
            tickScarletAmulet(maid, equipped);
            tickSilverBellCharm(maid, equipped);
            tickOnsenManjuBag(maid, equipped);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_EXHAUSTION.remove(id);
        NIGHT_SPARROW_PROVIDING.remove(id);
        SILVER_BELL_PROVIDING.remove(id);
        SILVER_BELL_CHIME_COOLDOWN.remove(id);
    }

    private static void tickExhaustionRefund(Player player, List<ItemStack> equipped) {
        float current = player.getFoodData().getExhaustionLevel();
        UUID id = player.getUUID();
        Float previous = LAST_EXHAUSTION.put(id, current);
        if (previous == null) {
            return;
        }
        float delta = current - previous;
        if (delta <= 0f) {
            return;
        }
        int count = countEquipped(equipped, ModItems.INSULATED_MEAL_BAG.get());
        double reduction = StackPolicy.MAX.mergeDouble(count, INSULATED_MEAL_BAG_DRAIN_REDUCTION);
        if (reduction <= 0) {
            return;
        }
        float refunded = current - (float) (delta * reduction);
        player.getFoodData().setExhaustion(refunded);
        LAST_EXHAUSTION.put(id, refunded);
    }

    /** Exposed for {@link com.touhoumaid.eatproperly.satiety.SatietyHandler} daily drain. */
    public static double insulatedMealBagDrainMultiplier(EntityMaid maid) {
        int count = countEquipped(MaidTrinketData.getEquipped(maid), ModItems.INSULATED_MEAL_BAG.get());
        double reduction = StackPolicy.MAX.mergeDouble(count, INSULATED_MEAL_BAG_DRAIN_REDUCTION);
        return Math.max(0.0, 1.0 - reduction);
    }

    private static void tickMoonAppleCandy(Player player, List<ItemStack> equipped) {
        CompoundTag root = buffRoot(player);
        if (!isEquipped(equipped, ModItems.MOON_APPLE_CANDY.get())) {
            root.remove(MOON_APPLE_LAST_TICK);
            return;
        }
        long now = player.level().getGameTime();
        if (!root.contains(MOON_APPLE_LAST_TICK)) {
            root.putLong(MOON_APPLE_LAST_TICK, now);
            return;
        }
        long elapsed = now - root.getLong(MOON_APPLE_LAST_TICK);
        if (elapsed <= 0) {
            return;
        }
        MoonAppleMath.Result result = MoonAppleMath.accrue(root.getDouble(MOON_APPLE_PROGRESS), elapsed);
        if (result.wholePoints() > 0) {
            player.getFoodData().eat(result.wholePoints(), 0.0F);
        }
        root.putDouble(MOON_APPLE_PROGRESS, result.leftoverProgress());
        root.putLong(MOON_APPLE_LAST_TICK, now);
    }

    private static void tickMoonAppleCandyMaid(EntityMaid maid, List<ItemStack> equipped) {
        CompoundTag root = buffRoot(maid);
        if (!isEquipped(equipped, ModItems.MOON_APPLE_CANDY.get())) {
            root.remove(MOON_APPLE_LAST_TICK);
            return;
        }
        long now = maid.level().getGameTime();
        if (!root.contains(MOON_APPLE_LAST_TICK)) {
            root.putLong(MOON_APPLE_LAST_TICK, now);
            return;
        }
        long elapsed = now - root.getLong(MOON_APPLE_LAST_TICK);
        if (elapsed <= 0) {
            return;
        }
        MoonAppleMath.Result result = MoonAppleMath.accrue(root.getDouble(MOON_APPLE_PROGRESS), elapsed);
        if (result.wholePoints() > 0) {
            SatietyData.addSatiety(maid, result.wholePoints());
        }
        root.putDouble(MOON_APPLE_PROGRESS, result.leftoverProgress());
        root.putLong(MOON_APPLE_LAST_TICK, now);
    }

    private static void tickScarletAmulet(LivingEntity entity, List<ItemStack> equipped) {
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) {
            return;
        }
        boolean shouldHave = isEquipped(equipped, ModItems.SCARLET_AMULET.get());
        boolean has = instance.getModifier(SCARLET_AMULET_MODIFIER_ID) != null;
        if (shouldHave && !has) {
            instance.addPermanentModifier(new AttributeModifier(
                    SCARLET_AMULET_MODIFIER_ID, "touhou_maid_eat_properly:scarlet_amulet",
                    SCARLET_AMULET_MAX_HEALTH_PERCENT, AttributeModifier.Operation.MULTIPLY_BASE));
        } else if (!shouldHave && has) {
            instance.removeModifier(SCARLET_AMULET_MODIFIER_ID);
        }
    }

    /**
     * Player-only night vision. Refresh near expiry only; clear only on unequip edge
     * so potions / other-mod NV sources are not stripped every tick.
     */
    private static void tickNightSparrowWrapper(Player player, List<ItemStack> equipped) {
        tickManagedEffect(player, isEquipped(equipped, ModItems.NIGHT_SPARROW_WRAPPER.get()),
                NIGHT_SPARROW_PROVIDING, MobEffects.NIGHT_VISION);
    }

    private static void tickSilverBellCharm(LivingEntity entity, List<ItemStack> equipped) {
        boolean wearing = isEquipped(equipped, ModItems.SILVER_BELL_CHARM.get());
        tickManagedEffect(entity, wearing, SILVER_BELL_PROVIDING, MobEffects.MOVEMENT_SPEED);
        tickSilverBellWalkChime(entity, wearing);
    }

    /**
     * Soft chime while walking with the charm equipped (matches item lore).
     * <p>
     * Timing: after a successful play, a 200-tick (~10s) cooldown counts down every server tick
     * (walking or not). When cooldown hits 0, the next tick that is on-ground and actually moving
     * plays again. Movement uses {@link LivingEntity#getDeltaMovement()} — not xo/zo deltas,
     * which are already zeroed by the time {@code LivingTickEvent} runs.
     */
    private static void tickSilverBellWalkChime(LivingEntity entity, boolean wearing) {
        UUID id = entity.getUUID();
        if (!wearing) {
            SILVER_BELL_CHIME_COOLDOWN.remove(id);
            return;
        }
        int cooldown = SILVER_BELL_CHIME_COOLDOWN.getOrDefault(id, 0);
        if (cooldown > 0) {
            SILVER_BELL_CHIME_COOLDOWN.put(id, cooldown - 1);
            return;
        }
        if (!entity.onGround()) {
            return;
        }
        double moveSq = entity.getDeltaMovement().horizontalDistanceSqr();
        if (moveSq < SILVER_BELL_MOVE_THRESHOLD_SQ) {
            return;
        }
        float pitch = 0.95F + entity.getRandom().nextFloat() * 0.1F;
        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                ModSounds.SILVER_BELL_CHIME.get(),
                SoundSource.PLAYERS,
                0.4F,
                pitch);
        SILVER_BELL_CHIME_COOLDOWN.put(id, SILVER_BELL_CHIME_INTERVAL_TICKS);
    }

    private static void tickManagedEffect(LivingEntity entity, boolean equippedNow,
                                          Set<UUID> providing, MobEffect effect) {
        UUID id = entity.getUUID();
        TrinketManagedEffectPolicy.Tick tick =
                TrinketManagedEffectPolicy.decide(equippedNow, providing.contains(id));
        if (tick.clear()) {
            entity.removeEffect(effect);
        }
        if (tick.provide()) {
            refreshTrinketEffect(entity, effect);
        }
        if (tick.providingAfter()) {
            providing.add(id);
        } else {
            providing.remove(id);
        }
    }

    private static void refreshTrinketEffect(LivingEntity entity, MobEffect effect) {
        MobEffectInstance current = entity.getEffect(effect);
        if (current != null && current.getDuration() > TRINKET_EFFECT_REFRESH_THRESHOLD_TICKS) {
            return;
        }
        entity.addEffect(new MobEffectInstance(
                effect, TRINKET_EFFECT_REFRESH_DURATION_TICKS, 0, true, false, true));
    }

    private static void tickOnsenManjuBag(LivingEntity entity, List<ItemStack> equipped) {
        if (!isEquipped(equipped, ModItems.ONSEN_MANJU_BAG.get())) {
            return;
        }
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        List<ItemStack> equipped = equippedOf(entity);
        if (equipped.isEmpty()) {
            return;
        }
        DamageSource source = event.getSource();

        if (isEquipped(equipped, ModItems.ONSEN_MANJU_BAG.get()) && source.is(DamageTypeTags.IS_FREEZING)) {
            event.setCanceled(true);
            return;
        }
        if (isEquipped(equipped, ModItems.LAKE_FAIRY_KEEPSAKE.get())) {
            Entity attacker = source.getEntity();
            if (attacker instanceof EntityFairy) {
                event.setCanceled(true);
                return;
            }
            if (attacker != null && ModList.get().isLoaded(EatProperlyMod.YOUKAI_MOD_ID)
                    && YoukaiHomecomingCompat.isSuppressedAttacker(attacker)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof EntityFairy)) {
            return;
        }
        LivingEntity target = event.getNewTarget();
        if (target == null || target.level().isClientSide()) {
            return;
        }
        if (isEquipped(equippedOf(target), ModItems.LAKE_FAIRY_KEEPSAKE.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (halvingInProgress) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        if (!isEquipped(equippedOf(entity), ModItems.SOBERING_RAISIN.get())) {
            return;
        }
        int halvedDuration = instance.getDuration() / 2;
        if (halvedDuration >= instance.getDuration()) {
            return;
        }
        halvingInProgress = true;
        try {
            entity.removeEffect(instance.getEffect());
            if (halvedDuration > 0) {
                entity.addEffect(new MobEffectInstance(instance.getEffect(), halvedDuration,
                        instance.getAmplifier(), instance.isAmbient(), instance.isVisible(), instance.showIcon()));
            }
        } finally {
            halvingInProgress = false;
        }
    }

    private static int countEquipped(List<ItemStack> equipped, Item item) {
        int count = 0;
        for (ItemStack stack : equipped) {
            if (!stack.isEmpty() && stack.is(item)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isEquipped(List<ItemStack> equipped, Item item) {
        return countEquipped(equipped, item) > 0;
    }

    private static CompoundTag buffRoot(LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }
}
