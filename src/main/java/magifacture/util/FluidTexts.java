package magifacture.util;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class FluidTexts {
    /**
     * Return user-friendly text for a fluid amount.
     */
    public static MutableText formatAmount(long amount) {
        // "0 mB"
        if (amount == 0) {
            return Text.translatable("magifacture.millibucket.amount", 0);
        }
        // "<1 mB"
        if (0 < amount && amount < 81) {
            return Text.translatable("magifacture.millibucket.less_than_1");
        }
        double fraction = amount / 81000D;
        long millibuckets = (long) Math.floor(fraction * 1000);
        // "250 mB"
        if (-1000 < millibuckets && millibuckets < 1000) {
            return Text.translatable("magifacture.millibucket.amount", millibuckets);
        }
        // "1.250 B"
        double buckets = millibuckets / 1000D;
        return Text.translatable("magifacture.bucket.amount", buckets);
    }

    public static MutableText formatAmountAndCapacity(long amount, long capacity) {
        return Text.translatable("magifacture.amount_and_capacity", formatAmount(amount), formatAmount(capacity));
    }

    public static List<Text> getTooltip(FluidVariant variant, long amount, TooltipContext context) {
        List<Text> tooltip = new ArrayList<>();

        tooltip.add(FluidVariantAttributes.getName(variant));
        FluidVariantRendering.getHandlerOrDefault(variant.getFluid()).appendTooltip(variant, tooltip, TooltipContext.BASIC);
        tooltip.add(formatAmount(amount).formatted(Formatting.GRAY));

        if (context.isAdvanced()) {
            tooltip.add(Text.literal(Registries.FLUID.getId(variant.getFluid()).toString()).formatted(Formatting.DARK_GRAY));
        }

        return tooltip;
    }

    public static List<Text> getTooltip(FluidVariant variant, long amount, long capacity, TooltipContext context) {
        List<Text> tooltip = new ArrayList<>();

        tooltip.add(FluidVariantAttributes.getName(variant));
        FluidVariantRendering.getHandlerOrDefault(variant.getFluid()).appendTooltip(variant, tooltip, context);
        tooltip.add(formatAmountAndCapacity(amount, capacity).formatted(Formatting.GRAY));

        if (context.isAdvanced()) {
            tooltip.add(Text.literal(Registries.FLUID.getId(variant.getFluid()).toString()).formatted(Formatting.DARK_GRAY));
        }

        return tooltip;
    }
}
