package com.nekomaster

import com.nekomaster.items.TimerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.BlockItem
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ItemRegistry {
    fun reg() {
        Registry.register(
            Registries.ITEM,
            Identifier("the-mod", "cfour"),
            BlockItem(ModBlocks.C4_BLOCK, FabricItemSettings())
        )
        Registry.register(
            Registries.ITEM,
            Identifier("the-mod", "wire"),
            BlockItem(ModBlocks.WIRE_BLOCK, FabricItemSettings())
        )
        Registry.register(
            Registries.ITEM,
            Identifier("the-mod", "timer"),
            ModItems.TIMER
        )
    }
}