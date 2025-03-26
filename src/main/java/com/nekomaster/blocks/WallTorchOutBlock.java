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

public class WallTorchOutBlock extends WallTorchBlock {
    // 单例实例
    public static final WallTorchOutBlock instance = new WallTorchOutBlock(
            FabricBlockSettings.copyOf(Blocks.WALL_TORCH)
                    .luminance(0)  // 无亮度
                    .dropsLike(Blocks.WALL_TORCH)  // 继承原版掉落
    );

    public WallTorchOutBlock(Settings settings) {
        super(settings, null);  // 不需要粒子效果
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.FLINT_AND_STEEL)) {
            // 恢复为点燃的墙上火把（保持原方向）
            world.setBlockState(pos, Blocks.WALL_TORCH.getDefaultState()
                            .with(WallTorchBlock.FACING, state.get(FACING)),
                    Block.NOTIFY_ALL);

            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static BlockState getBlockDefaultState() {
        return instance.getDefaultState();
    }
}