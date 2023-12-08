package magifacture.fluid;

import magifacture.Magifacture;
import magifacture.block.MoltenMagicCrystalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;

public abstract class MoltenMagicCrystalFluid extends MoltenSubstanceFluid {
    @Override
    public Fluid getFlowing() {
        return MoltenMagicCrystalFluid.Flowing.INSTANCE;
    }

    @Override
    public Fluid getStill() {
        return MoltenMagicCrystalFluid.Still.INSTANCE;
    }

    @Override
    public Item getBucketItem() {
        return Magifacture.MOLTEN_MAGIC_CRYSTAL_BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return MoltenMagicCrystalBlock.INSTANCE.getDefaultState() //
                .with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    public static class Flowing extends MoltenMagicCrystalFluid {
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

    public static class Still extends MoltenMagicCrystalFluid {
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
}
