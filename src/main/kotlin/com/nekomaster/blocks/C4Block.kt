import com.nekomaster.ModItems
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class C4Block(settings: Settings) : Block(settings) {
    init {
        defaultState = stateManager.defaultState
            .with(ARMED, false)
            .with(FACING, Direction.NORTH)
    }

    companion object {
        val ARMED: BooleanProperty = BooleanProperty.of("armed")
        val FACING: DirectionProperty = DirectionProperty.of("facing", Direction.Type.HORIZONTAL)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(ARMED, FACING)
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos,
                       player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val stack = player.getStackInHand(hand)
        if (player.isSneaking) {
            // Shift+right-click to disarm
            if (state.get(ARMED)) {
                world.setBlockState(pos, state.with(ARMED, false))
                player.sendMessage(Text.literal("C4 disarmed").formatted(Formatting.GOLD), true)
                return ActionResult.SUCCESS
            }
        } else if (stack.isOf(ModItems.TIMER)) {
            // Right-click with TimerItem to arm
            world.setBlockState(pos, state.with(ARMED, true))
            if (!player.isCreative) stack.decrement(1)
            world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1f, 1f)
            player.sendMessage(Text.literal("C4 armed").formatted(Formatting.RED), true)
            return ActionResult.SUCCESS
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    fun explode(world: World, pos: BlockPos) {
        if (!world.isClient) {
            // Remove block
            world.removeBlock(pos, false)

            // Create explosion
            world.createExplosion(
                null, // Explosion source entity
                pos.x + 0.5,
                pos.y + 0.5,
                pos.z + 0.5,
                8.0f, // Explosion power
                true, // Causes block damage
                World.ExplosionSourceType.TNT
            )
        }
    }
}