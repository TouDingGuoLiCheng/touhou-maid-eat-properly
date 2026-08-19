package com.touhoumaid.eatproperly.item;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.effect.HappinessTiming;
import com.touhoumaid.eatproperly.effect.ModEffects;
import com.touhoumaid.eatproperly.item.reward.BoundLunchboxItem;
import com.touhoumaid.eatproperly.item.reward.BoundRewardItem;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxItem;
import com.touhoumaid.eatproperly.item.reward.EdibleBoundRewardItem;
import com.touhoumaid.eatproperly.item.reward.ExtraMealPermitItem;
import com.touhoumaid.eatproperly.item.reward.FloorDecorRewardItem;
import com.touhoumaid.eatproperly.item.reward.HandwrittenLetterItem;
import com.touhoumaid.eatproperly.item.reward.HypnosisNoteItem;
import com.touhoumaid.eatproperly.item.reward.MealBellItem;
import com.touhoumaid.eatproperly.item.reward.WallHangRewardItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EatProperlyMod.MOD_ID);

    /** All milestone reward items for creative tab / iteration. */
    public static final List<RegistryObject<Item>> REWARD_ITEMS = new ArrayList<>();

    public static final RegistryObject<Item> LUNCHBOX = ITEMS.register(
            "lunchbox",
            () -> new LunchboxItem(new Item.Properties().stacksTo(1))
    );

    /** Maid-only Order Book bauble (not a milestone reward; altar-crafted). */
    public static final RegistryObject<Item> ORDER_BOOK = ITEMS.register(
            "order_book",
            () -> new OrderBookItem(new Item.Properties().stacksTo(1))
    );

    /** Maid-only Food Permit bauble: mark containers for open food fetch. */
    public static final RegistryObject<Item> FOOD_PERMIT = ITEMS.register(
            "food_permit",
            () -> new FoodPermitItem(new Item.Properties().stacksTo(1))
    );

    // --- TOKEN_LOW ---
    public static final RegistryObject<Item> FORGET_ME_NOT_BOUQUET = reward("forget_me_not_bouquet", 2);
    public static final RegistryObject<Item> PRAISE_CERTIFICATE =
            rewardCustom("praise_certificate", props -> new WallHangRewardItem(props, 2));
    public static final RegistryObject<Item> THANKS_NOTE_EMPTY_BOX = reward("thanks_note_empty_box", 2);
    public static final RegistryObject<Item> PAPER_CRANE = reward("paper_crane", 2);
    public static final RegistryObject<Item> MEAL_BELL =
            rewardCustom("meal_bell", props -> new MealBellItem(props, 2));
    // Derived leftovers registered first so the edible tokens below can reference them directly.
    public static final RegistryObject<Item> CRUMPLED_CANDY_WRAPPER = reward("crumpled_candy_wrapper", 2);
    public static final RegistryObject<Item> CAKE_NOTE = reward("cake_note", 2);
    public static final RegistryObject<Item> THANKS_CANDY = rewardEdible(
            "thanks_candy", candyFoodProperties(), 2, () -> CRUMPLED_CANDY_WRAPPER.get());
    public static final RegistryObject<Item> HALF_STRAWBERRY_CAKE = rewardEdible(
            "half_strawberry_cake", cakeFoodProperties(), 2, () -> CAKE_NOTE.get());
    public static final RegistryObject<Item> FIELD_PHOTO = reward("field_photo", 2);
    public static final RegistryObject<Item> BANNER_HONEST_BOSS =
            rewardCustom("banner_honest_boss", props -> new WallHangRewardItem(props, 2));

    // --- TOKEN_MID ---
    public static final RegistryObject<Item> HEART_GEM = reward("heart_gem", 2);
    public static final RegistryObject<Item> HANDWRITTEN_LETTER =
            rewardCustom("handwritten_letter", props -> new HandwrittenLetterItem(props, 5));
    public static final RegistryObject<Item> MOONLIGHT_SHARD = reward("moonlight_shard", 2);
    public static final RegistryObject<Item> TWO_TONE_HAIR_TIE = reward("two_tone_hair_tie");
    public static final RegistryObject<Item> EMBROIDERED_HANDKERCHIEF = reward("embroidered_handkerchief", 2);
    public static final RegistryObject<Item> PAIRED_CUP = reward("paired_cup", 2);
    public static final RegistryObject<Item> UNOPENED_SOUVENIR = reward("unopened_souvenir");
    public static final RegistryObject<Item> PRESSED_DIARY_PAGE = reward("pressed_diary_page");
    public static final RegistryObject<Item> BLOCK_FIGURINE =
            rewardCustom("block_figurine", props -> new FloorDecorRewardItem(props, 2));
    public static final RegistryObject<Item> SILVER_BELL_CHARM = reward("silver_bell_charm", 1);

    // --- TOKEN_HIGH ---
    public static final RegistryObject<Item> WARM_AMULET = reward("warm_amulet", 2);
    public static final RegistryObject<Item> ETERNAL_MEAL_TICKET = reward("eternal_meal_ticket", 2);
    public static final RegistryObject<Item> HEART_KNOT = reward("heart_knot", 2);
    public static final RegistryObject<Item> SUGAR_JAR = reward("sugar_jar", 2);
    public static final RegistryObject<Item> UNNAMED_RING = reward("unnamed_ring", 2);
    public static final RegistryObject<Item> PLAIN_POUCH = reward("plain_pouch", 2);

    // --- Milestone rewards with custom item classes (TOKEN_MID / BUFF pools) ---
    public static final RegistryObject<Item> BOUND_LUNCHBOX = rewardCustom("bound_lunchbox", BoundLunchboxItem::new);
    public static final RegistryObject<Item> DUAL_LUNCHBOX = rewardCustom("dual_lunchbox", DualLunchboxItem::new);

    /** Altar-crafted tools (not milestone rewards). */
    public static final RegistryObject<Item> EXTRA_MEAL_PERMIT = ITEMS.register(
            "extra_meal_permit",
            () -> new ExtraMealPermitItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HYPNOSIS_NOTE = ITEMS.register(
            "hypnosis_note",
            () -> new HypnosisNoteItem(new Item.Properties().stacksTo(1)));

    // --- BUFF ---
    public static final RegistryObject<Item> HEART_SEASONING = reward("heart_seasoning");
    public static final RegistryObject<Item> INSULATED_MEAL_BAG = reward("insulated_meal_bag");
    public static final RegistryObject<Item> MOON_APPLE_CANDY = reward("moon_apple_candy");
    public static final RegistryObject<Item> SCARLET_AMULET = reward("scarlet_amulet");
    public static final RegistryObject<Item> NIGHT_SPARROW_WRAPPER = reward("night_sparrow_wrapper");
    public static final RegistryObject<Item> SWEET_SOFT_CANDY = reward("sweet_soft_candy");
    public static final RegistryObject<Item> ONSEN_MANJU_BAG = reward("onsen_manju_bag");
    public static final RegistryObject<Item> SOBERING_RAISIN = reward("sobering_raisin");
    public static final RegistryObject<Item> LAKE_FAIRY_KEEPSAKE = reward("lake_fairy_keepsake");

    private ModItems() {
    }

    private static RegistryObject<Item> reward(String id) {
        return reward(id, 1);
    }

    private static RegistryObject<Item> reward(String id, int loreLineCount) {
        RegistryObject<Item> obj = ITEMS.register(
                id,
                () -> new BoundRewardItem(new Item.Properties().stacksTo(1), loreLineCount)
        );
        REWARD_ITEMS.add(obj);
        return obj;
    }

    /** Milestone rewards with real behavior classes instead of the generic {@link BoundRewardItem} placeholder. */
    private static RegistryObject<Item> rewardCustom(String id, Function<Item.Properties, Item> factory) {
        RegistryObject<Item> obj = ITEMS.register(id, () -> factory.apply(new Item.Properties().stacksTo(1)));
        REWARD_ITEMS.add(obj);
        return obj;
    }

    /** Edible tokens (candy / cake) that grant Happiness and leave behind a bound leftover item. */
    private static RegistryObject<Item> rewardEdible(
            String id, FoodProperties food, int loreLineCount, Supplier<? extends Item> leftover) {
        RegistryObject<Item> obj = ITEMS.register(id,
                () -> new EdibleBoundRewardItem(new Item.Properties().stacksTo(1).food(food), loreLineCount, leftover));
        REWARD_ITEMS.add(obj);
        return obj;
    }

    private static FoodProperties candyFoodProperties() {
        return new FoodProperties.Builder()
                .nutrition(5)
                .saturationMod(0.6F)
                .effect(() -> new MobEffectInstance(ModEffects.HAPPINESS.get(), HappinessTiming.DURATION_TICKS), 1.0F)
                .alwaysEat()
                .build();
    }

    private static FoodProperties cakeFoodProperties() {
        return new FoodProperties.Builder()
                .nutrition(10)
                .saturationMod(0.6F)
                .effect(() -> new MobEffectInstance(ModEffects.HAPPINESS.get(), HappinessTiming.DURATION_TICKS), 1.0F)
                .alwaysEat()
                .build();
    }
}
