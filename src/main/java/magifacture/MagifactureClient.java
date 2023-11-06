package magifacture;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MagifactureClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupFluidTextures(ExperienceFluid.INSTANCE);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), Magifacture.INFUSED_GLASS);
    }

    private static void setupFluidTextures(Fluid fluid) {
        Identifier id = Registries.FLUID.getId(fluid);
        Identifier still = new Identifier(id.getNamespace(), "block/" + id.getPath());
        Identifier flow = new Identifier(id.getNamespace(), "block/" + id.getPath() + "_flow");
        FluidRenderHandlerRegistry.INSTANCE.register(fluid, new SimpleFluidRenderHandler(still, flow, 0xFF_FFFFFF));
    }
}