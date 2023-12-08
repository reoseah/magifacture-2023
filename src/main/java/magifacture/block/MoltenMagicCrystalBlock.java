package magifacture.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;

public class MoltenMagicCrystalBlock extends Block {
    public static final Block INSTANCE = new MoltenMagicCrystalBlock(FabricBlockSettings.of().mapColor(MapColor.PINK).luminance(15).noCollision());

    protected MoltenMagicCrystalBlock(Settings settings) {
        super(settings);
    }
}
