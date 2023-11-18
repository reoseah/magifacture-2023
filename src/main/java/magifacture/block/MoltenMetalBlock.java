package magifacture.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MoltenMetalBlock extends FluidBlock {
    public final Block solidified;

    public MoltenMetalBlock(FlowableFluid fluid, Block solidified, Settings settings) {
        super(fluid, settings);
        this.solidified = solidified;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public boolean receiveNeighborFluids(World world, BlockPos pos, BlockState state) {
        if (this.solidified != null && world.getFluidState(pos).isStill()) {
            for (Direction direction : FLOW_DIRECTIONS) {
                BlockPos aside = pos.offset(direction.getOpposite());
                if (world.getFluidState(aside).isIn(FluidTags.WATER)) {
                    world.setBlockState(pos, this.solidified.getDefaultState());
                    this.playExtinguishSound(world, pos);
                    return false;
                }
            }
        }
        return super.receiveNeighborFluids(world, pos, state);
    }
}
