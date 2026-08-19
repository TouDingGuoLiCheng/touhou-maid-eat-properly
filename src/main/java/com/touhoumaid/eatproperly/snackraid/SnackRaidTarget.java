package com.touhoumaid.eatproperly.snackraid;

import net.minecraft.core.BlockPos;

/** A reachable container and the block the maid should stand on to interact. */
public record SnackRaidTarget(BlockPos containerPos, BlockPos standPos) {
}
