package com.nekomaster

import com.nekomaster.items.TimerItem
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity

object ModItems {
    val TIMER = TimerItem(FabricItemSettings()
        .maxCount(1)
        .rarity(Rarity.UNCOMMON)
    )
}