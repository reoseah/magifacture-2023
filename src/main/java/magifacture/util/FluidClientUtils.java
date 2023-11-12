package magifacture.util;

import magifacture.mixin.FluidAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FluidClientUtils {
    public static MutableText getName(Fluid fluid) {
        // try to translate fluid.<modid>.<name>
        // in particular, make empty volume "Empty" instead of "Air"
        Identifier id = Registries.FLUID.getId(fluid);
        String key = Util.createTranslationKey("fluid", id);
        if (Language.getInstance().hasTranslation(key)) {
            return Text.translatable(key);
        }
        // try to get the name of the associated block,
        // works for water/lava and most modded fluids
        BlockState blockstate = ((FluidAccessor) fluid).callToBlockState(fluid.getDefaultState());
        if (blockstate != null) {
            return Text.translatable(blockstate.getBlock().getTranslationKey());
        }
        // return block.<modid>.<name> anyway
        return Text.translatable(Util.createTranslationKey("block", id));
    }

    public static MutableText getAmountText(int amount) {
        // welp yep it did happen to me so this is now here
        if (amount < 0) {
            return Text.literal("-").append(getAmountText(-amount));
        }
        // "0 mB"
        if (amount == 0) {
            return Text.translatable("magifacture.millibucket.amount", 0);
        }
        // "<1 mB"
        if (amount < 81) {
            return Text.translatable("magifacture.millibucket.less_than_1");
        }
        double fraction = amount / 81000D;
        long millibuckets = (long) Math.floor(fraction * 1000);
        // "250 mB"
        if (millibuckets < 1000) {
            return Text.translatable("magifacture.millibucket.amount", millibuckets);
        }
        // "1.250 B"
        double buckets = millibuckets / 1000D;
        return Text.translatable("magifacture.bucket.amount", buckets);
    }

    public static final String AMOUNT_AND_CAPACITY = "magifacture.amount_and_capacity";

    public static MutableText getAmountTextWithCapacity(int amount, int maxAmount) {
        return Text.translatable(AMOUNT_AND_CAPACITY, getAmountText(amount), getAmountText(maxAmount)).formatted(Formatting.GRAY);
    }

    public static List<Text> getTooltipWithCapacity(Fluid fluid, int amount, int maxAmount) {
        return Arrays.asList(getName(fluid), getAmountTextWithCapacity(amount, maxAmount));
    }

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

    public static int getColor(FluidVariant variant, BlockRenderView world, BlockPos pos) {
        return FluidVariantRendering.getColor(variant, world, pos);
    }

    public static @NotNull Sprite getStillTexture(FluidVariant volume) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(volume.getFluid());
        if (handler == null) {
            return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(MissingSprite.getMissingSpriteId());
        }
        Sprite[] sprites = handler.getFluidSprites(null, null, volume.getFluid().getDefaultState());
        if (sprites == null || sprites[0] == null) {
            return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(MissingSprite.getMissingSpriteId());
        }
        return sprites[0];
    }

}
