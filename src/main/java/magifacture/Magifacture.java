package magifacture;

import magifacture.block.*;
import magifacture.block.entity.AlembicBlockEntity;
import magifacture.block.entity.CrematoriumBlockEntity;
import magifacture.block.entity.InfuserBlockEntity;
import magifacture.fluid.ExperienceFluid;
import magifacture.fluid.MoltenGoldFluid;
import magifacture.item.ExperienceBucketItem;
import magifacture.recipe.CremationRecipe;
import magifacture.recipe.InfusionRecipe;
import magifacture.recipe.ItemFillingInfusionRecipe;
import magifacture.recipe.SimpleCremationRecipe;
import magifacture.screen.AlembicScreenHandler;
import magifacture.screen.CrematoriumScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Magifacture implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("magifacture");

    public static final Block MOLTEN_GOLD = new MoltenMetalBlock(MoltenGoldFluid.Still.INSTANCE, Blocks.GOLD_BLOCK, AbstractBlock.Settings.create().liquid().mapColor(MapColor.BRIGHT_RED).strength(100F).dropsNothing());

    public static final Block INFUSED_STONE = new Block(AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(2F));
    public static final Block MAGIC_CRYSTAL_ORE = new Block(AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).strength(3F).luminance(state -> 10));
    public static final Block INFUSED_GLASS = new GlassBlock(AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).strength(2F).luminance(state -> 10).nonOpaque().sounds(BlockSoundGroup.GLASS));
    public static final Block HIGHLY_INFUSED_GLASS = new Block(AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(2F).luminance(state -> 14).nonOpaque().sounds(BlockSoundGroup.GLASS));
    public static final Block MAGIC_CRYSTAL_BLOCK = new Block(AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).strength(3F, 5F).luminance(state -> 15).sounds(BlockSoundGroup.AMETHYST_BLOCK));

    public static final Item MOLTEN_GOLD_BUCKET = new BucketItem(MoltenGoldFluid.Still.INSTANCE, new Item.Settings().recipeRemainder(Items.BUCKET));
    public static final Item ASH = new Item(new Item.Settings());

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing...");

        Registry.register(Registries.BLOCK, "magifacture:experience", ExperienceBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:molten_gold", MOLTEN_GOLD);
        Registry.register(Registries.BLOCK, "magifacture:crematorium", CrematoriumBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:alembic", AlembicBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:infuser", InfuserBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:terra_enchanter", TerraEnchanterBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:infused_stone", INFUSED_STONE);
        Registry.register(Registries.BLOCK, "magifacture:magic_crystal_ore", MAGIC_CRYSTAL_ORE);
        Registry.register(Registries.BLOCK, "magifacture:infused_glass", INFUSED_GLASS);
        Registry.register(Registries.BLOCK, "magifacture:highly_infused_glass", HIGHLY_INFUSED_GLASS);
        Registry.register(Registries.BLOCK, "magifacture:magic_crystal_block", MAGIC_CRYSTAL_BLOCK);

        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magifacture:alembic", AlembicBlockEntity.TYPE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magifacture:crematorium", CrematoriumBlockEntity.TYPE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magifacture:infuser", InfuserBlockEntity.TYPE);

        FluidStorage.SIDED.registerForBlockEntity((be, side) -> side != Direction.UP ? be.getTank() : null, AlembicBlockEntity.TYPE);
        FluidStorage.SIDED.registerForBlockEntity((be, side) -> side != Direction.UP ? be.getTank() : null, CrematoriumBlockEntity.TYPE);
        FluidStorage.SIDED.registerForBlockEntity((be, side) -> be.getTank(), InfuserBlockEntity.TYPE);

        Registry.register(Registries.FLUID, "magifacture:experience", ExperienceFluid.INSTANCE);
        Registry.register(Registries.FLUID, "magifacture:molten_gold", MoltenGoldFluid.Still.INSTANCE);
        Registry.register(Registries.FLUID, "magifacture:flowing_molten_gold", MoltenGoldFluid.Flowing.INSTANCE);

        Registry.register(Registries.ITEM, "magifacture:crematorium", CrematoriumBlock.ITEM);
        Registry.register(Registries.ITEM, "magifacture:alembic", AlembicBlock.ITEM);
        Registry.register(Registries.ITEM, "magifacture:infuser", InfuserBlock.ITEM);
        Registry.register(Registries.ITEM, "magifacture:terra_enchanter", TerraEnchanterBlock.ITEM);
        Registry.register(Registries.ITEM, "magifacture:infused_stone", new BlockItem(INFUSED_STONE, new Item.Settings()));
        Registry.register(Registries.ITEM, "magifacture:magic_crystal_ore", new BlockItem(MAGIC_CRYSTAL_ORE, new Item.Settings().rarity(Rarity.UNCOMMON)));
        Registry.register(Registries.ITEM, "magifacture:infused_glass", new BlockItem(INFUSED_GLASS, new Item.Settings()));
        Registry.register(Registries.ITEM, "magifacture:highly_infused_glass", new BlockItem(HIGHLY_INFUSED_GLASS, new Item.Settings().rarity(Rarity.UNCOMMON)));
        Registry.register(Registries.ITEM, "magifacture:magic_crystal_block", new BlockItem(MAGIC_CRYSTAL_BLOCK, new Item.Settings().rarity(Rarity.RARE)));

        Registry.register(Registries.ITEM, "magifacture:experience_bucket", ExperienceBucketItem.INSTANCE);
        Registry.register(Registries.ITEM, "magifacture:molten_gold_bucket", MOLTEN_GOLD_BUCKET);
        Registry.register(Registries.ITEM, "magifacture:ash", ASH);

        CompostingChanceRegistry.INSTANCE.add(ASH, 0.05F);

        DispenserBehavior bucketBehavior = new ItemDispenserBehavior() {
            private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                FluidModificationItem item = (FluidModificationItem) stack.getItem();
                BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                World world = pointer.world();
                if (item.placeFluid(null, world, blockPos, null)) {
                    item.onEmptied(null, world, stack, blockPos);
                    return new ItemStack(Items.BUCKET);
                }
                return this.fallbackBehavior.dispense(pointer, stack);
            }
        };
        DispenserBlock.registerBehavior(MOLTEN_GOLD_BUCKET, bucketBehavior);

        // noinspection UnstableApiUsage
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(context -> new EmptyItemFluidStorage(context, Items.EXPERIENCE_BOTTLE, ExperienceFluid.INSTANCE, FluidConstants.BOTTLE));
        // noinspection UnstableApiUsage
        FluidStorage.combinedItemApiProvider(Items.EXPERIENCE_BOTTLE).register(context -> new FullItemFluidStorage(context, Items.GLASS_BOTTLE, FluidVariant.of(ExperienceFluid.INSTANCE), FluidConstants.BOTTLE));

        ItemGroup itemGroup = FabricItemGroup.builder() //
                .displayName(Text.translatable("itemGroup.magifacture")) //
                .icon(() -> new ItemStack(CrematoriumBlock.INSTANCE)) //
                .entries((parameters, entries) -> { //
                    entries.add(CrematoriumBlock.INSTANCE);
                    entries.add(AlembicBlock.INSTANCE);
                    entries.add(InfuserBlock.INSTANCE);
                    entries.add(TerraEnchanterBlock.INSTANCE);
                    entries.add(INFUSED_STONE);
                    entries.add(MAGIC_CRYSTAL_ORE);
                    entries.add(INFUSED_GLASS);
                    entries.add(HIGHLY_INFUSED_GLASS);
                    entries.add(MAGIC_CRYSTAL_BLOCK);

                    entries.add(ExperienceBucketItem.INSTANCE);
                    entries.add(MOLTEN_GOLD_BUCKET);
                    entries.add(ASH);
                }) //
                .build();
        Registry.register(Registries.ITEM_GROUP, "magifacture:main", itemGroup);

        Registry.register(Registries.RECIPE_TYPE, "magifacture:cremation", CremationRecipe.TYPE);
        Registry.register(Registries.RECIPE_TYPE, "magifacture:infusion", InfusionRecipe.TYPE);

        Registry.register(Registries.RECIPE_SERIALIZER, "magifacture:cremation", SimpleCremationRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magifacture:item_filling_infusion", ItemFillingInfusionRecipe.SERIALIZER);

        Registry.register(Registries.SCREEN_HANDLER, "magifacture:crematorium", CrematoriumScreenHandler.TYPE);
        Registry.register(Registries.SCREEN_HANDLER, "magifacture:alembic", AlembicScreenHandler.TYPE);
    }
}