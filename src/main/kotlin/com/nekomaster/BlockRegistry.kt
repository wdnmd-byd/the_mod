package com.nekomaster

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object BlockRegistry {
    fun reg() {
        Registry.register(
            Registries.BLOCK,
            Identifier("the-mod", "cfour"),
            ModBlocks.C4_BLOCK
        )
        Registry.register(
            Registries.BLOCK,
            Identifier("the-mod", "wire"),
            ModBlocks.WIRE_BLOCK
        )
    }
}