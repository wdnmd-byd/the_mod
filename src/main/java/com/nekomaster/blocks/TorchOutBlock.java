package com.nekomaster.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TorchOutBlock extends Block {
    public static final TorchOutBlock INSTANCE = new TorchOutBlock(
            FabricBlockSettings.copyOf(Blocks.TORCH)
                    .luminance(0)
    );

    public TorchOutBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        // 关键修改！用isOf()而不是==
        if (stack.isOf(Items.FLINT_AND_STEEL)) {
            // 根据当前方块状态决定恢复哪种火把
            BlockState newState = state.contains(WallTorchBlock.FACING)
                    ? Blocks.WALL_TORCH.getDefaultState()
                        .with(WallTorchBlock.FACING, state.get(WallTorchBlock.FACING))
                    : Blocks.TORCH.getDefaultState();

            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static BlockState getDefualtState() {
        return INSTANCE.getDefaultState();
    }
}