package com.nekomaster

import C4Block
import com.nekomaster.blocks.TorchOutBlock
import com.nekomaster.blocks.WallTorchOutBlock
import com.nekomaster.blocks.WireBlock
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Blocks
import net.minecraft.sound.BlockSoundGroup


object ModBlocks {
    val C4_BLOCK = C4Block(FabricBlockSettings.create()
        .strength(4f)
        .requiresTool()
    )
    val WIRE_BLOCK = WireBlock(FabricBlockSettings.copyOf(Blocks.STONE)
        .strength(1.5f)
        .requiresTool()
        .sounds(BlockSoundGroup.METAL)
        .solid()
    )
    val TORCH_OUT = TorchOutBlock.INSTANCE // This object is already registered in TorchBlocks.java
    val WALL_TORCH_OUT = WallTorchOutBlock.instance // This object is already registered in TorchBlocks.java
}