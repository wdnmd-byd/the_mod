package com.nekomaster.events

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.Blocks
import net.minecraft.world.World

object DiamondExplode {
    fun register() {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, _ ->
            if (state.block == Blocks.DIAMOND_ORE) {
                world.createExplosion(null,
                    pos.x.toDouble(),
                    pos.y.toDouble(),
                    pos.z.toDouble(),
                    5.0f,
                    World.ExplosionSourceType.BLOCK
                );
            }
        }
    }
}