package com.touhoumaid.eatproperly.entity;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EatProperlyMod.MOD_ID);

    public static final RegistryObject<EntityType<WallHangEntity>> WALL_HANG = ENTITY_TYPES.register(
            "wall_hang",
            () -> EntityType.Builder.<WallHangEntity>of(WallHangEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(EatProperlyMod.MOD_ID + ":wall_hang")
    );

    public static final RegistryObject<EntityType<FloorDecorEntity>> FLOOR_DECOR = ENTITY_TYPES.register(
            "floor_decor",
            () -> EntityType.Builder.<FloorDecorEntity>of(FloorDecorEntity::new, MobCategory.MISC)
                    .sized(0.7F, 0.9F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(EatProperlyMod.MOD_ID + ":floor_decor")
    );

    private ModEntities() {
    }
}
