package com.nekomaster.functions;

import com.nekomaster.blocks.TorchOutBlock;
import com.nekomaster.blocks.WallTorchOutBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TorchBurnout {
    private static final int CHECK_INTERVAL = 100;
    private static final int CHECK_RADIUS = 16;
    private static final float BASE_CHANCE = 0.005f;
    private static final float RAIN_MULTIPLIER = 3f;

    public static void init() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (!world.isClient && world.getTime() % CHECK_INTERVAL == 0) {
                for (PlayerEntity player : world.getPlayers()) {
                    checkNearbyTorches(world, player.getBlockPos());
                }
            }
        });
    }

    private static boolean isTorch(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.TORCH || block == Blocks.WALL_TORCH;
    }

    private static void extinguishTorch(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.TORCH) {
            world.setBlockState(pos, TorchOutBlock.INSTANCE.getDefaultState(), Block.NOTIFY_ALL);
        }
        else if (state.getBlock() == Blocks.WALL_TORCH) {
            world.setBlockState(pos, WallTorchOutBlock.instance.getDefaultState()
                            .with(WallTorchBlock.FACING, state.get(WallTorchBlock.FACING)),
                    Block.NOTIFY_ALL);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 1.0f);
    }

    private static void checkNearbyTorches(World world, BlockPos center) {
        BlockPos.iterateOutwards(center, CHECK_RADIUS, CHECK_RADIUS, CHECK_RADIUS)
                .forEach(pos -> {
                    if (world.isChunkLoaded(pos)) {
                        BlockState state = world.getBlockState(pos);
                        if (isTorch(state) && shouldBurnout(world, pos)) {
                            extinguishTorch(world, pos);
                        }
                    }
                });
    }

    private static boolean shouldBurnout(World world, BlockPos pos) {
        if (world.getFluidState(pos.down()).isIn(FluidTags.WATER)) {
            return true;
        }

        float chance = BASE_CHANCE;
        if (world.isRaining() && world.isSkyVisible(pos.up())) {
            chance *= RAIN_MULTIPLIER;
        }
        return world.random.nextFloat() < chance;
    }
}