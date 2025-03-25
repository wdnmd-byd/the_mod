package com.nekomaster

import com.nekomaster.events.DiamondExplode
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.loader.impl.launch.FabricMixinBootstrap.init
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix
import net.minecraft.screen.PropertyDelegate
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.DirectionProperty


object ModBlocks {
    val C4_BLOCK = Block(FabricBlockSettings.create()
        .strength(5.0f)
        .requiresTool()
    )

    private val FACING : DirectionProperty = DirectionProperty.of("facing")
    private val POWERED : BooleanProperty = BooleanProperty.of("powered")
    val WIRE = Block(
        FabricBlockSettings.copy(Blocks.REDSTONE_WIRE).strength(5.0f)
    )
}