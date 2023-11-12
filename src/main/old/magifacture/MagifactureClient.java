package reoseah.magifacture;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import reoseah.magifacture.client.screen.AlembicScreen;
import reoseah.magifacture.client.screen.CrematoriumScreen;
import reoseah.magifacture.client.screen.InfusionTableScreen;
import reoseah.magifacture.fluid.ExperienceFluid;
import reoseah.magifacture.screen.AlembicScreenHandler;
import reoseah.magifacture.screen.CrematoriumScreenHandler;
import reoseah.magifacture.screen.InfusionTableScreenHandler;

public class MagifactureClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupFluidTextures(ExperienceFluid.INSTANCE);

        HandledScreens.register(CrematoriumScreenHandler.TYPE, CrematoriumScreen::new);
        HandledScreens.register(AlembicScreenHandler.TYPE, AlembicScreen::new);
        HandledScreens.register(InfusionTableScreenHandler.TYPE, InfusionTableScreen::new);
    }

    private static void setupFluidTextures(Fluid fluid) {
        Identifier id = Registries.FLUID.getId(fluid);
        Identifier still = new Identifier(id.getNamespace(), "block/" + id.getPath());
        Identifier flow = new Identifier(id.getNamespace(), "block/" + id.getPath() + "_flow");
        FluidRenderHandlerRegistry.INSTANCE.register(fluid, new SimpleFluidRenderHandler(still, flow, 0xFF_FFFFFF));
    }
}
