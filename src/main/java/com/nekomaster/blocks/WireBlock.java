package com.nekomaster.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;

public class WireBlock extends Block {
    // 红石信号强度属性 (0-15)
    public static final IntProperty POWER = RedstoneWireBlock.POWER;
    public static final BooleanProperty POWERED= BooleanProperty.of("powered");

    public WireBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER, POWERED);
    }

    // 当相邻方块更新时
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            // 更新红石信号
            this.updatePower(world, pos, state);

            // 通知相邻方块更新
            for(Direction direction : Direction.values()) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }
        }
    }

    // 计算并更新红石信号强度
    private void updatePower(World world, BlockPos pos, BlockState state) {
        int maxPower = 0;

        // 检查6个方向的输入信号
        for(Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            int neighborPower = world.getEmittedRedstonePower(neighborPos, dir);
            maxPower = Math.max(maxPower, neighborPower);
        }

        // 衰减1格强度
        int newPower = Math.max(0, maxPower - 1);

        // 如果信号强度变化则更新状态
        if (newPower != state.get(POWER)) {
            world.setBlockState(pos, state.with(POWER, newPower), Block.NOTIFY_ALL);
        }
    }

    // 获取输出的红石信号强度
    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    // 阻止从某些方向输出信号
    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
}