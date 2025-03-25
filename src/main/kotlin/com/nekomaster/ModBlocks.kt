package com.nekomaster

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block

object ModBlocks {
    val C4_BLOCK = Block(FabricBlockSettings.create()
        .strength(5.0f)
        .requiresTool()
    )
}