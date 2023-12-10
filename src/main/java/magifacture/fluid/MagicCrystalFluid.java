package magifacture.fluid;

import magifacture.block.MoltenMagicCrystalBlock;
import magifacture.item.MagicCrystalBucketItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;

public abstract class MagicCrystalFluid extends LavaLikeFluid {
    public static final String HEAT_KEY = "Heat";
    public static final String POWER_KEY = "Power";
    public static final String PURITY_KEY = "Purity";

    public static final String POWER_TRANSLATION_KEY = "magifacture.magic_crystal.power";
    public static final String PURITY_TRANSLATION_KEY = "magifacture.magic_crystal.purity";
    public static final String HEAT_TRANSLATION_KEY = "magifacture.magic_crystal.heat";

    public static final NumberFormat STATS_FORMAT = NumberFormat.getInstance();

    static {
        STATS_FORMAT.setMaximumFractionDigits(2);
    }

    @Override
    public Fluid getFlowing() {
        return MagicCrystalFluid.Flowing.INSTANCE;
    }

    @Override
    public Fluid getStill() {
        return MagicCrystalFluid.Still.INSTANCE;
    }

    @Override
    public Item getBucketItem() {
        return MagicCrystalBucketItem.INSTANCE;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return MoltenMagicCrystalBlock.INSTANCE.getDefaultState() //
                .with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    public static void appendTooltip(@Nullable NbtCompound nbt, List<Text> tooltip) {
        if (nbt != null) {
            tooltip.add(Text.translatable(POWER_TRANSLATION_KEY, STATS_FORMAT.format(nbt.getFloat(POWER_KEY))).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable(PURITY_TRANSLATION_KEY, STATS_FORMAT.format(nbt.getFloat(PURITY_KEY))).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable(HEAT_TRANSLATION_KEY, STATS_FORMAT.format(nbt.getFloat(HEAT_KEY))).formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.translatable(POWER_TRANSLATION_KEY, 0).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable(PURITY_TRANSLATION_KEY, 0).formatted(Formatting.GRAY));
            tooltip.add(Text.translatable(HEAT_TRANSLATION_KEY, 0).formatted(Formatting.GRAY));
        }
    }

    public static @Nullable NbtCompound copyNbt(@Nullable NbtCompound nbt) {
        if (nbt == null) {
            return null;
        }
        NbtCompound copy = new NbtCompound();
        copy.putFloat(POWER_KEY, nbt.getFloat(POWER_KEY));
        copy.putFloat(PURITY_KEY, nbt.getFloat(PURITY_KEY));
        copy.putFloat(HEAT_KEY, nbt.getFloat(HEAT_KEY));
        return copy;
    }

    public static class Flowing extends MagicCrystalFluid {
        public static final FlowableFluid INSTANCE = new Flowing();

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState fluidState) {
            return fluidState.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends MagicCrystalFluid {
        public static final FlowableFluid INSTANCE = new Still();

        @Override
        public int getLevel(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return true;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class RenderHandler implements FluidVariantRenderHandler {
        public static final FluidVariantRenderHandler INSTANCE = new RenderHandler();

        @Override
        public void appendTooltip(FluidVariant fluidVariant, List<Text> tooltip, TooltipContext tooltipContext) {
            NbtCompound nbt = fluidVariant.getNbt();
            MagicCrystalFluid.appendTooltip(nbt, tooltip);
        }
    }
}
