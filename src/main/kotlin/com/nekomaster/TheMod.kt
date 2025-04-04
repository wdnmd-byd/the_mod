package com.nekomaster

import com.nekomaster.events.DiamondExplode
import net.fabricmc.api.ModInitializer
import net.minecraft.block.Block
import org.slf4j.LoggerFactory

object TheMod : ModInitializer {
    private val logger = LoggerFactory.getLogger("the-mod")
	override fun onInitialize() {
		logger.info("Hello Fabric world!")
		DiamondExplode.register()
		ItemRegistry.reg()
		BlockRegistry.reg()
	}
}