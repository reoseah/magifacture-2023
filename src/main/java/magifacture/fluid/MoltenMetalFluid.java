package magifacture.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;

public abstract class MoltenMetalFluid extends FlowableFluid {
    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == this.getStill() || fluid == this.getFlowing();
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        this.playExtinguishEvent(world, pos);
    }

    private void playExtinguishEvent(WorldAccess world, BlockPos pos) {
        world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return world.getDimension().ultrawarm() ? 4 : 3;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return world.getDimension().ultrawarm() ? 1 : 2;
    }

    @Override
    public int getTickRate(WorldView world) {
        return world.getDimension().ultrawarm() ? 10 : 20;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }
}
