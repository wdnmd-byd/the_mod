package com.nekomaster.blocks;

import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.*;

public class WireBlock extends Block {
    // 状态属性
    public static final IntProperty POWER = RedstoneWireBlock.POWER;
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    public static final BooleanProperty UPDATING = BooleanProperty.of("updating");

    // 性能优化常量
    private static final Direction[] UPDATE_ORDER = {
            Direction.WEST, Direction.EAST,
            Direction.NORTH, Direction.SOUTH,
            Direction.DOWN, Direction.UP
    };
    private static final int MAX_CHAIN_LENGTH = 16;
    private static final int FORCE_UPDATE_RANGE = 2;

    public WireBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(POWER, 0)
                .with(POWERED, false)
                .with(UPDATING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER, POWERED, UPDATING);
    }

    // ========== 核心更新逻辑 ==========
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient && !state.get(UPDATING)) {
            // 按优先级调度更新
            for (Direction dir : UPDATE_ORDER) {
                if (pos.offset(dir).equals(fromPos)) {
                    int delay = getDirectionalDelay(dir, world.getBlockState(fromPos));
                    world.scheduleBlockTick(pos, this, delay);
                    break;
                }
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(UPDATING)) return;

        // 标记为正在更新防止递归
        world.setBlockState(pos, state.with(UPDATING, true), Block.NOTIFY_LISTENERS);

        try {
            int newPower = calculateOptimalPower(world, pos);
            if (newPower != state.get(POWER)) {
                // 更新状态并记录变化
                world.setBlockState(pos, state.with(POWER, newPower).with(POWERED, newPower > 0),
                        Block.NOTIFY_LISTENERS);

                // 智能传播更新
                scheduleStrategicUpdates(world, pos, newPower);
            }
        } finally {
            // 清除更新标记
            world.setBlockState(pos, state.with(UPDATING, false), Block.NOTIFY_LISTENERS);
        }
    }

    // ========== 智能信号计算 ==========
    private int calculateOptimalPower(World world, BlockPos pos) {
        // 阶段1：快速检查直接电源
        int directPower = getDirectInputPower(world, pos);
        if (directPower >= 15) return 15;

        // 阶段2：检查导线网络
        int networkPower = calculateNetworkPower(world, pos, MAX_CHAIN_LENGTH, new HashSet<>());

        return Math.max(directPower, networkPower);
    }

    private int getDirectInputPower(World world, BlockPos pos) {
        int maxPower = 0;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = world.getBlockState(neighborPos);

            // 忽略其他导线
            if (!(neighborState.getBlock() instanceof WireBlock)) {
                int power = world.getEmittedRedstonePower(neighborPos, dir);
                maxPower = Math.max(maxPower, power);
            }
        }
        return maxPower;
    }

    private int calculateNetworkPower(World world, BlockPos pos, int remainingDepth, Set<BlockPos> visited) {
        if (remainingDepth <= 0 || visited.contains(pos)) return 0;

        visited.add(pos);
        int maxPower = 0;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = world.getBlockState(neighborPos);

            if (neighborState.getBlock() instanceof WireBlock) {
                // 递归计算网络功率（衰减1）
                int chainPower = calculateNetworkPower(world, neighborPos, remainingDepth - 1, visited);
                maxPower = Math.max(maxPower, chainPower - 1);
            }
        }

        return MathHelper.clamp(maxPower, 0, 15);
    }

    // ========== 更新传播策略 ==========
    private void scheduleStrategicUpdates(World world, BlockPos pos, int newPower) {
        // 1. 优先更新下游（功率减小的方向）
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = world.getBlockState(neighborPos);

            if (neighborState.getBlock() instanceof WireBlock) {
                int neighborPower = neighborState.get(POWER);
                if (newPower > neighborPower) {
                    world.scheduleBlockTick(neighborPos, this, 1); // 标准延迟
                } else if (newPower < neighborPower) {
                    world.scheduleBlockTick(neighborPos, this, 0); // 立即更新
                }
            }
        }

        // 2. 强制更新附近红石元件
        if (newPower > 0) {
            updateNearbyRedstone(world, pos);
        }
    }

    private void updateNearbyRedstone(World world, BlockPos centerPos) {
        for (int x = -FORCE_UPDATE_RANGE; x <= FORCE_UPDATE_RANGE; x++) {
            for (int y = -FORCE_UPDATE_RANGE; y <= FORCE_UPDATE_RANGE; y++) {
                for (int z = -FORCE_UPDATE_RANGE; z <= FORCE_UPDATE_RANGE; z++) {
                    BlockPos checkPos = centerPos.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (state.emitsRedstonePower() && !(state.getBlock() instanceof WireBlock)) {
                        world.updateNeighbor(checkPos, this, centerPos);
                    }
                }
            }
        }
    }

    // ========== 辅助方法 ==========
    private int getDirectionalDelay(Direction dir, BlockState neighborState) {
        // 红石块等强电源优先处理
        if (neighborState.getBlock() == Blocks.REDSTONE_BLOCK) {
            return 0;
        }
        return dir.getAxis().isHorizontal() ? 1 : 2;
    }

    // ========== 红石基础功能 ==========
    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!world.isClient && !moved && !state.isOf(newState.getBlock())) {
            updateNearbyRedstone(world, pos);
        }
    }
}