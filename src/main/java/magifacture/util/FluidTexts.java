package magifacture.util;

import magifacture.mixin.FluidAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.Util;

import java.util.Arrays;
import java.util.List;

public class FluidTexts {
    /**
     * Get the name of a fluid, translated if possible.
     */
    public static MutableText getName(Fluid fluid) {
        // try to translate fluid.<modid>.<name>
        // in particular, make empty fluid "Empty" instead of "Air"
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

    public static List<Text> getTooltip(Fluid fluid, long amount, long capacity) {
        return Arrays.asList(getName(fluid), formatAmountAndCapacity(amount, capacity).formatted(Formatting.GRAY));
    }
}
