import com.nekomaster.ModItems
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
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
            if (stack.isOf(ModItems.TIMER) && !state.get(ARMED)) {
                world.setBlockState(pos, state.with(ARMED, true))
                if (!player.isCreative) stack.decrement(1)
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1f, 1f)
                world.scheduleBlockTick(pos, this, 200) // 10秒后爆炸
                return ActionResult.SUCCESS
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    @Deprecated("Deprecated in Java")
    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (!world.isClient && state.get(ARMED)) {
            // 先移除方块
            world.removeBlock(pos, false)

            // 创建爆炸
            world.createExplosion(
                null, // 爆炸来源实体
                pos.x + 0.5,
                pos.y + 0.5,
                pos.z + 0.5,
                8.0f, // 爆炸威力
                true, // 是否破坏方块
                World.ExplosionSourceType.TNT
            )

            // 爆炸粒子效果
            world.spawnParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                pos.x + 0.5,
                pos.y + 0.5,
                pos.z + 0.5,
                5,
                0.5, 0.5, 0.5,
                0.1
            )
        }
    }
}