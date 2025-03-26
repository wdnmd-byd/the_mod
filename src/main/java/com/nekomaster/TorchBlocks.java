package com.nekomaster;

import com.nekomaster.blocks.TorchOutBlock;
import com.nekomaster.blocks.WallTorchOutBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static net.minecraft.registry.Registries.*;

public class TorchBlocks {
    // 使用Holder而不是直接实例
    private static RegistryEntry<Block> TORCH_OUT;
    private static RegistryEntry<Block> WALL_TORCH_OUT;

    public static void register() {
        // 1. 先创建未注册的实例
        TorchOutBlock torchOut = new TorchOutBlock(FabricBlockSettings.copyOf(Blocks.TORCH).luminance(0));
        WallTorchOutBlock wallTorchOut = new WallTorchOutBlock(FabricBlockSettings.copyOf(Blocks.WALL_TORCH).luminance(0));

        // 2. 正式注册
        TORCH_OUT = Registry.registerReference(
                Registries.BLOCK,
                new Identifier("nekomaster", "torch_out"),
                torchOut
        );

        WALL_TORCH_OUT = Registry.registerReference(
                Registries.BLOCK,
                new Identifier("nekomaster", "wall_torch_out"),
                wallTorchOut
        );

        // 3. 注册物品
        Registry.register(Registries.ITEM, new Identifier("nekomaster", "torch_out"),
                new BlockItem(torchOut, new FabricItemSettings()));
        Registry.register(Registries.ITEM, new Identifier("nekomaster", "wall_torch_out"),
                new BlockItem(wallTorchOut, new FabricItemSettings()));
    }

    // 安全获取方法
    public static BlockState getTorchOutState() {
        return TORCH_OUT.getDefaultState();
    }

    public static BlockState getWallTorchOutState(Direction facing) {
        return WALL_TORCH_OUT.getBlockDefaultState().with(WallTorchBlock.FACING, facing);
    }
}