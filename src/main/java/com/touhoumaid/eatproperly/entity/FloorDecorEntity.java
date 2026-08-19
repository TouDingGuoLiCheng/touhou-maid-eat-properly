package com.touhoumaid.eatproperly.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/**
 * Floor decoration (block figurine): sits on a block, yaw faces the placing player.
 * Yaw is synched — otherwise the client keeps 0 and the renderer offset lands in the wrong block.
 */
public class FloorDecorEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(FloorDecorEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DATA_YAW =
            SynchedEntityData.defineId(FloorDecorEntity.class, EntityDataSerializers.FLOAT);

    public FloorDecorEntity(EntityType<? extends FloorDecorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FloorDecorEntity(Level level, BlockPos floorPos, float yRot, ItemStack stack) {
        this(ModEntities.FLOOR_DECOR.get(), level);
        double[] feet = FloorDecorPlacement.feetOnBlockTop(floorPos.getX(), floorPos.getY(), floorPos.getZ());
        setPos(feet[0], feet[1], feet[2]);
        setDecorYaw(yRot);
        setItem(stack);
        refreshDecorBounds();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_YAW, 0.0F);
    }

    public ItemStack getItem() {
        return this.entityData.get(DATA_ITEM);
    }

    public void setItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM, stack.copyWithCount(1));
    }

    public float getDecorYaw() {
        return this.entityData.get(DATA_YAW);
    }

    public void setDecorYaw(float yaw) {
        this.entityData.set(DATA_YAW, yaw);
        setYRot(yaw);
        this.yRotO = yaw;
    }

    private void refreshDecorBounds() {
        double x = getX();
        double y = getY();
        double z = getZ();
        setBoundingBox(new AABB(x - 0.35D, y, z - 0.35D, x + 0.35D, y + 0.9D, z + 0.35D));
    }

    @Override
    public void tick() {
        super.tick();
        refreshDecorBounds();
        if (!level().isClientSide && !hasSupport()) {
            breakAndDrop(null);
        }
    }

    private boolean hasSupport() {
        BlockPos below = BlockPos.containing(getX(), getY() - 0.05D, getZ());
        return !level().getBlockState(below).isAir();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (!level().isClientSide && !isRemoved()) {
            breakAndDrop(source.getEntity());
            return true;
        }
        return false;
    }

    private void breakAndDrop(@javax.annotation.Nullable Entity breaker) {
        playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
        if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (!(breaker instanceof Player player && player.getAbilities().instabuild)) {
                ItemStack stack = getItem();
                if (!stack.isEmpty()) {
                    spawnAtLocation(stack.copy());
                }
            }
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Item")) {
            setItem(ItemStack.of(tag.getCompound("Item")));
        }
        setDecorYaw(tag.getFloat("DecorYaw"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            tag.put("Item", stack.save(new CompoundTag()));
        }
        tag.putFloat("DecorYaw", getDecorYaw());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeFloat(getDecorYaw());
        buf.writeItem(getItem());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        setDecorYaw(buf.readFloat());
        setXRot(0.0F);
        setItem(buf.readItem());
        refreshDecorBounds();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_YAW.equals(key)) {
            float yaw = getDecorYaw();
            setYRot(yaw);
            this.yRotO = yaw;
        }
        super.onSyncedDataUpdated(key);
    }
}
