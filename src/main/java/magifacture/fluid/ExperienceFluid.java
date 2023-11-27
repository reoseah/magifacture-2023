package magifacture.fluid;

import magifacture.block.ExperienceBlock;
import magifacture.item.ExperienceBucketItem;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class ExperienceFluid extends Fluid {
    public static final Fluid INSTANCE = new ExperienceFluid();
    public static final int MILLIBUCKET_PER_XP = 20;
    public static final int XP_PER_BUCKET = 1000 / MILLIBUCKET_PER_XP;

    @Override
    public Item getBucketItem() {
        return ExperienceBucketItem.INSTANCE;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ExperienceBlock.INSTANCE.getDefaultState();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        return Vec3d.ZERO;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 0;
    }

    @Override
    protected float getBlastResistance() {
        return 0;
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    public float getHeight(FluidState state) {
        return 0;
    }

    @Override
    public boolean isStill(FluidState state) {
        return true;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }
}
