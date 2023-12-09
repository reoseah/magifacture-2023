package magifacture.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class FluidRendering {
    public static void drawFluidColumn(DrawContext context, FluidVariant fluid, int x, int y, int height, int z) {
        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(new BufferBuilder(256));
        drawFluidColumn(context, immediate, fluid, x, y, height, z);
        immediate.draw();
    }

    public static void drawFluidColumn(DrawContext context, VertexConsumerProvider consumers, FluidVariant variant, int x, int y, int height, int z) {
        Sprite sprite = getStillTexture(variant);
        int color = FluidVariantRendering.getColor(variant);

        int r = ((color >> 16) & 255);
        int g = ((color >> 8) & 255);
        int b = (color & 255);

        VertexConsumer consumer = ItemRenderer.getDirectItemGlintConsumer(consumers, RenderLayer.getSolid(), true, false);

        MinecraftClient.getInstance().getTextureManager().bindTexture(sprite.getAtlasId());
        Matrix4f positionMatrix = context.getMatrices().peek().getPositionMatrix();
        Matrix3f normalMatrix = context.getMatrices().peek().getNormalMatrix();

        int tiles = MathHelper.ceil(height / 16F);
        for (int i = 0; i < tiles; i++) {
            @SuppressWarnings("UnnecessaryLocalVariable") float x0 = x;
            float y0 = y + i * 16;

            float x1 = x0 + 16;
            float y1 = y0 + Math.min(height - i * 16, 16);

            float u0 = sprite.getMinU();
            float v0 = sprite.getMinV();
            float u1 = sprite.getMaxU();
            float v1 = sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * ((y1 - y0) / 16F);

            consumer.vertex(positionMatrix, x0, y1, z).color(r, g, b, 0xFF).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(0xFF).normal(normalMatrix, 0, 0, 1).next();
            consumer.vertex(positionMatrix, x1, y1, z).color(r, g, b, 0xFF).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(0xFF).normal(normalMatrix, 0, 0, 1).next();
            consumer.vertex(positionMatrix, x1, y0, z).color(r, g, b, 0xFF).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(0xFF).normal(normalMatrix, 0, 0, 1).next();
            consumer.vertex(positionMatrix, x0, y0, z).color(r, g, b, 0xFF).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(0xFF).normal(normalMatrix, 0, 0, 1).next();
        }
    }

    public static @NotNull Sprite getStillTexture(FluidVariant variant) {
        Sprite sprite = FluidVariantRendering.getSprite(variant);

        return sprite != null ? sprite : MinecraftClient.getInstance() //
                .getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE) //
                .apply(MissingSprite.getMissingSpriteId());
    }
}
