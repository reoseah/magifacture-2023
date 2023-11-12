package reoseah.magifacture;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reoseah.magifacture.block.AlembicBlock;
import reoseah.magifacture.block.CrematoriumBlock;
import reoseah.magifacture.block.InfusionTableBlock;
import reoseah.magifacture.block.MagelightBlock;
import reoseah.magifacture.block.entity.AlembicBlockEntity;
import reoseah.magifacture.block.entity.CrematoriumBlockEntity;
import reoseah.magifacture.block.entity.InfusionTableBlockEntity;
import reoseah.magifacture.fluid.ExperienceFluid;
import reoseah.magifacture.item.MagifactureItems;
import reoseah.magifacture.recipe.*;
import reoseah.magifacture.screen.AlembicScreenHandler;
import reoseah.magifacture.screen.CrematoriumScreenHandler;
import reoseah.magifacture.screen.InfusionTableScreenHandler;

public class Magifacture implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("magifacture");

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, "magifacture:crematorium", CrematoriumBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:infusion_table", InfusionTableBlock.INSTANCE);
        Registry.register(Registries.BLOCK, "magifacture:magelight", MagelightBlock.INSTANCE);

        Registry.register(Registries.ITEM, "magifacture:crematorium", CrematoriumBlock.ITEM);
        Registry.register(Registries.ITEM, "magifacture:infusion_table", InfusionTableBlock.ITEM);
        Registry.register(Registries.ITEM, "magifacture:ash", MagifactureItems.ASH);
        Registry.register(Registries.ITEM, "magifacture:magelight", MagelightBlock.ITEM);


        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magifacture:crematorium", CrematoriumBlockEntity.TYPE);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "magifacture:infusion_table", InfusionTableBlockEntity.TYPE);

        Registry.register(Registries.RECIPE_TYPE, "magifacture:cremation", CremationRecipe.TYPE);
        Registry.register(Registries.RECIPE_TYPE, "magifacture:infusion", InfusionRecipe.TYPE);

        Registry.register(Registries.RECIPE_SERIALIZER, "magifacture:cremation", SimpleCremationRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magifacture:shaped_infusion", ShapedInfusionRecipe.SERIALIZER);
        Registry.register(Registries.RECIPE_SERIALIZER, "magifacture:item_filling_infusion", ItemFillingInfusionRecipe.SERIALIZER);

        Registry.register(Registries.SCREEN_HANDLER, "magifacture:crematorium", CrematoriumScreenHandler.TYPE);
        Registry.register(Registries.SCREEN_HANDLER, "magifacture:infusion_table", InfusionTableScreenHandler.TYPE);

//        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP).register(entries -> {
//            entries.add(CrematoriumBlock.ITEM);
//            entries.add(AlembicBlock.ITEM);
//            entries.add(InfusionTableBlock.ITEM);
//            entries.add(MagifactureItems.ASH);
//            entries.add(MagelightBlock.ITEM);
//        });
        CompostingChanceRegistry.INSTANCE.add(MagifactureItems.ASH, 0.05F);
        // noinspection UnstableApiUsage
        FluidStorage.combinedItemApiProvider(Items.GLASS_BOTTLE).register(context -> new EmptyItemFluidStorage(context, Items.EXPERIENCE_BOTTLE, ExperienceFluid.INSTANCE, FluidConstants.BOTTLE));
        // noinspection UnstableApiUsage
        FluidStorage.combinedItemApiProvider(Items.EXPERIENCE_BOTTLE).register(context -> new FullItemFluidStorage(context, Items.GLASS_BOTTLE, FluidVariant.of(ExperienceFluid.INSTANCE), FluidConstants.BOTTLE));
        // noinspection UnstableApiUsage

    }
}