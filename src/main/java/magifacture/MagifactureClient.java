package magifacture;

import magifacture.fluid.ExperienceFluid;
import magifacture.fluid.MoltenGoldFluid;
import magifacture.screen.AlembicScreen;
import magifacture.screen.AlembicScreenHandler;
import magifacture.screen.CrematoriumScreen;
import magifacture.screen.CrematoriumScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MagifactureClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupFluidTextures(ExperienceFluid.INSTANCE, "experience");
        setupFluidTextures(MoltenGoldFluid.Still.INSTANCE, "molten_gold");
        setupFluidTextures(MoltenGoldFluid.Flowing.INSTANCE, "molten_gold");

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), //
                Magifacture.INFUSED_GLASS);

        HandledScreens.register(CrematoriumScreenHandler.TYPE, CrematoriumScreen::new);
        HandledScreens.register(AlembicScreenHandler.TYPE, AlembicScreen::new);
    }

    private static void setupFluidTextures(Fluid fluid, String name) {
        Identifier id = Registries.FLUID.getId(fluid);
        Identifier still = new Identifier(id.getNamespace(), "block/" + name);
        Identifier flow = new Identifier(id.getNamespace(), "block/" + name + "_flow");
        FluidRenderHandlerRegistry.INSTANCE.register(fluid, new SimpleFluidRenderHandler(still, flow, 0xFF_FFFFFF));
    }
}