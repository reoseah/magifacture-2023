package magifacture;

import magifacture.fluid.ExperienceFluid;
import magifacture.fluid.MoltenMagicCrystalFluid;
import magifacture.screen.*;
import magifacture.screen.client.AlembicScreen;
import magifacture.screen.client.CrematoriumScreen;
import magifacture.screen.client.InfuserScreen;
import magifacture.screen.client.MixingColumnScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class MagifactureClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupFluidTextures(ExperienceFluid.INSTANCE, "experience");
        setupFluidTextures(MoltenMagicCrystalFluid.Still.INSTANCE, "molten_magic_crystal");
        setupFluidTextures(MoltenMagicCrystalFluid.Flowing.INSTANCE, "molten_magic_crystal");

        FluidVariantRendering.register(MoltenMagicCrystalFluid.Still.INSTANCE, MoltenMagicCrystalFluid.RenderHandler.INSTANCE);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), //
                Magifacture.INFUSED_GLASS);

        HandledScreens.register(CrematoriumScreenHandler.TYPE, CrematoriumScreen::new);
        HandledScreens.register(AlembicScreenHandler.TYPE, AlembicScreen::new);
        HandledScreens.register(InfuserScreenHandler.TYPE, InfuserScreen::new);
        HandledScreens.register(MixingColumnScreenHandler.TYPE, MixingColumnScreen::new);
    }

    private static void setupFluidTextures(Fluid fluid, String name) {
        Identifier id = Registries.FLUID.getId(fluid);
        Identifier still = new Identifier(id.getNamespace(), "block/" + name);
        Identifier flow = new Identifier(id.getNamespace(), "block/" + name + "_flow");
        FluidRenderHandlerRegistry.INSTANCE.register(fluid, new SimpleFluidRenderHandler(still, flow, 0xFF_FFFFFF));
    }
}