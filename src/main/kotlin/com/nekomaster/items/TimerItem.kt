package com.nekomaster.items

import C4Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.WeakHashMap

class TimerItem(settings: Settings) : Item(settings) {
    private val boundC4Map = WeakHashMap<PlayerEntity, BlockPos?>()
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos
        val player = context.player
        val state = world.getBlockState(pos)
        if (state.block !is C4Block) return ActionResult.PASS
        if (!world.isClient) {
            // 检查是否已武装
            if (state.get(C4Block.ARMED)) {
                player?.sendMessage(
                    Text.literal("Timer already installed")
                        .formatted(Formatting.RED),
                    true
                )
                return ActionResult.FAIL
            }

            // 绑定到当前C4
            boundC4Map[player] = pos
            player?.sendMessage(
                Text.literal("成功绑定C4到位置: (${pos.x}, ${pos.y}, ${pos.z})")
                    .formatted(Formatting.GREEN),
                true
            )

            // 播放安装音效
            world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_LEVER_CLICK,
                SoundCategory.BLOCKS,
                1f,
                1.5f
            )
        }

        return ActionResult.success(world.isClient)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val boundPos = boundC4Map[user]

        if (user.isSneaking) {
            // Shift+右键解除绑定
            boundC4Map.remove(user)
            user.sendMessage(
                Text.literal("已解除C4绑定").formatted(Formatting.GOLD),
                true
            )
            return TypedActionResult.success(stack)
        }

        if (boundPos == null) {
            user.sendMessage(
                Text.literal("你他妈还没绑定C4呢！").formatted(Formatting.RED),
                true
            )
            return TypedActionResult.fail(stack)
        }

        if (!world.isClient) {
            val state = world.getBlockState(boundPos)

            // 再次确认是C4方块且未被激活
            if (state.block !is C4Block || state.get(C4Block.ARMED)) {
                user.sendMessage(
                    Text.literal("绑定的C4无效或已激活！").formatted(Formatting.RED),
                    true
                )
                boundC4Map.remove(user)
                return TypedActionResult.fail(stack)
            }

            // 激活C4倒计时
            world.setBlockState(
                boundPos,
                state.with(C4Block.ARMED, true)
            )

            // 10秒倒计时 (20 ticks/sec)
            (world as? ServerWorld)?.let { serverWorld ->
                serverWorld.scheduleBlockTick(
                    boundPos,
                    state.block,
                    200
                )
            }

            // 播放激活音效
            world.playSound(
                null,
                boundPos,
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.BLOCKS,
                1f,
                2f
            )

            // 消耗物品 (非创造模式)
            if (!user.isCreative) {
                stack.decrement(1)
            }

            // 解除绑定
            boundC4Map.remove(user)
        }

        return TypedActionResult.success(stack)
    }

    // 添加物品描述
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        tooltip.add(Text.literal("右键点击C4方块进行绑定").formatted(Formatting.GRAY))
        tooltip.add(Text.literal("再次右键启动10秒倒计时").formatted(Formatting.GRAY))
        tooltip.add(Text.literal("Shift+右键解除绑定").formatted(Formatting.DARK_GRAY))
    }
}
