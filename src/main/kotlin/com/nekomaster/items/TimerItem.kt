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
            // Check if already armed
            if (state.get(C4Block.ARMED)) {
                player?.sendMessage(
                    Text.literal("Timer already installed")
                        .formatted(Formatting.RED),
                    true
                )
                return ActionResult.FAIL
            }

            // Bind to current C4
            boundC4Map[player] = pos
            player?.sendMessage(
                Text.literal("Successfully bound C4 to position: (${pos.x}, ${pos.y}, ${pos.z})")
                    .formatted(Formatting.GREEN),
                true
            )

            // Play installation sound
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
            // Shift+right-click to unbind
            boundC4Map.remove(user)
            user.sendMessage(
                Text.literal("C4 unbound").formatted(Formatting.GOLD),
                true
            )
            return TypedActionResult.success(stack)
        }

        if (boundPos == null) {
            user.sendMessage(
                Text.literal("No C4 bound!").formatted(Formatting.RED),
                true
            )
            return TypedActionResult.fail(stack)
        }

        if (!world.isClient) {
            val state = world.getBlockState(boundPos)

            // Confirm it's a C4 block and armed
            if (state.block !is C4Block || !state.get(C4Block.ARMED)) {
                user.sendMessage(
                    Text.literal("Bound C4 is invalid or not armed!").formatted(Formatting.RED),
                    true
                )
                boundC4Map.remove(user)
                return TypedActionResult.fail(stack)
            }

            // Trigger explosion
            (state.block as C4Block).explode(world, boundPos)

            // Unbind
            boundC4Map.remove(user)
        }

        return TypedActionResult.success(stack)
    }

    // Add item description
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltip.add(Text.literal("Right-click on C4 block to bind").formatted(Formatting.GRAY))
        tooltip.add(Text.literal("Right-click again to trigger explosion").formatted(Formatting.GRAY))
        tooltip.add(Text.literal("Shift+right-click to unbind").formatted(Formatting.DARK_GRAY))
    }
}