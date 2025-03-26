package com.nekomaster.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.world.World

class DetonatorItem(settings: Settings) : Item(
    settings.maxCount(16).rarity(Rarity.UNCOMMON)
) {
    companion object {
        private const val MIN_EXPLODE_FALL_DISTANCE = 2.5f
        private const val EXPLOSION_POWER = 3.0f
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (!world.isClient && entity is ItemEntity) {
            if (entity.fallDistance > MIN_EXPLODE_FALL_DISTANCE && entity.isOnGround) {
                (world as? ServerWorld)?.let { serverWorld ->
                    serverWorld.createExplosion(
                        null,
                        entity.x,
                        entity.y,
                        entity.z,
                        EXPLOSION_POWER,
                        true,
                        World.ExplosionSourceType.TNT
                    )
                    entity.discard()
                }
            }
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        tooltip.add(Text.literal("Warning: Explodes on impact after falling!").formatted(Formatting.RED))
        tooltip.add(Text.literal("Safe fall distance: <${MIN_EXPLODE_FALL_DISTANCE} blocks").formatted(Formatting.GRAY))
    }
}