package com.touhoumaid.eatproperly.entity;

import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * Wall-hung reward decoration (certificate 1×1, banner 1×2).
 * Spawn data must sync hang pos + facing; otherwise the client keeps default SOUTH and a wrong hitbox.
 * Certificate uses pixel height 12 (like item frames) so offs=0 and the entity centers in the hang block.
 */
public class WallHangEntity extends HangingEntity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(WallHangEntity.class, EntityDataSerializers.ITEM_STACK);

    public WallHangEntity(EntityType<? extends WallHangEntity> type, Level level) {
        super(type, level);
    }

    public WallHangEntity(Level level, BlockPos pos, Direction direction, ItemStack stack) {
        super(ModEntities.WALL_HANG.get(), level, pos);
        setItem(stack);
        setDirection(direction);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM, stack.copyWithCount(1));
    }

    public boolean isBanner() {
        return getItem().is(ModItems.BANNER_HONEST_BOSS.get());
    }

    @Override
    public int getWidth() {
        return WallHangPlacement.hangPixelWidth();
    }

    @Override
    public int getHeight() {
        return WallHangPlacement.hangPixelHeight(isBanner());
    }

    @Override
    public void dropItem(@Nullable Entity breaker) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (breaker instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            spawnAtLocation(stack.copy());
        }
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            tag.put("Item", stack.save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        ItemStack stack = ItemStack.EMPTY;
        if (tag.contains("Item")) {
            stack = ItemStack.of(tag.getCompound("Item"));
        }
        setItem(stack);
        super.readAdditionalSaveData(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.getDirection().get3DDataValue());
        buf.writeItem(getItem());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        int facing = buf.readVarInt();
        ItemStack stack = buf.readItem();
        setItem(stack);
        setDirection(Direction.from3DDataValue(facing));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_ITEM.equals(key)) {
            recalculateBoundingBox();
        }
        super.onSyncedDataUpdated(key);
    }
}
