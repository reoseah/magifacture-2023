package magifacture;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Magifacture implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("magifacture");

    public static final Block INFUSED_STONE = new Block(AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(2F));
    public static final Block MAGIC_CRYSTAL_ORE = new Block(AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).strength(3F).luminance(state -> 10));
    public static final Block MAGIC_CRYSTAL_BLOCK = new Block(AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).strength(3F, 5F).luminance(state -> 14));
    public static final Block INFUSED_GLASS = new GlassBlock(AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(2F).luminance(state -> 10).nonOpaque().sounds(BlockSoundGroup.GLASS));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing...");

        Registry.register(Registries.BLOCK, "magifacture:experience", ExperienceBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:infused_stone", INFUSED_STONE);
        Registry.register(Registries.BLOCK, "magifacture:magic_crystal_ore", MAGIC_CRYSTAL_ORE);
        Registry.register(Registries.BLOCK, "magifacture:magic_crystal_block", MAGIC_CRYSTAL_BLOCK);
        Registry.register(Registries.BLOCK, "magifacture:infused_glass", INFUSED_GLASS);

        Registry.register(Registries.ITEM, "magifacture:experience_bucket", ExperienceBucketItem.INSTANCE);
        Registry.register(Registries.ITEM, "magifacture:infused_stone", new BlockItem(INFUSED_STONE, new Item.Settings()));
        Registry.register(Registries.ITEM, "magifacture:magic_crystal_ore", new BlockItem(MAGIC_CRYSTAL_ORE, new Item.Settings().rarity(Rarity.UNCOMMON)));
        Registry.register(Registries.ITEM, "magifacture:magic_crystal_block", new BlockItem(MAGIC_CRYSTAL_BLOCK, new Item.Settings().rarity(Rarity.UNCOMMON)));
        Registry.register(Registries.ITEM, "magifacture:infused_glass", new BlockItem(INFUSED_GLASS, new Item.Settings()));

        ItemGroup itemGroup = FabricItemGroup.builder() //
                .displayName(Text.translatable("itemGroup.magifacture")) //
                .icon(() -> new ItemStack(Magifacture.MAGIC_CRYSTAL_BLOCK)) //
                .entries((parameters, entries) -> { //
                    entries.add(INFUSED_STONE);
                    entries.add(MAGIC_CRYSTAL_ORE);
                    entries.add(MAGIC_CRYSTAL_BLOCK);
                    entries.add(INFUSED_GLASS);

                    entries.add(ExperienceBucketItem.INSTANCE);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magifacture:main", itemGroup);

        Registry.register(Registries.FLUID, "magifacture:experience", ExperienceFluid.INSTANCE);
    }
}