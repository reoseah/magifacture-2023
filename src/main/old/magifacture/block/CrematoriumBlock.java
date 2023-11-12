package reoseah.magifacture.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import reoseah.magifacture.block.entity.CrematoriumBlockEntity;

public class CrematoriumBlock extends MagifactureBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public static final Block INSTANCE = new CrematoriumBlock(FabricBlockSettings.of(Material.METAL, MapColor.GRAY).strength(3F).luminance(state -> state.get(Properties.LIT) ? 15 : 0));
    public static final Item ITEM = new BlockItem(INSTANCE, new FabricItemSettings());

    protected CrematoriumBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrematoriumBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, CrematoriumBlockEntity.TYPE, CrematoriumBlockEntity::tickServer);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(LIT)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;
            if (random.nextFloat() < 0.1F) {
                world.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1F, 1F, false);
            }

            Direction facing = state.get(FACING);
            Direction.Axis axis = facing.getAxis();
            double dx = axis == Direction.Axis.X ? facing.getOffsetX() * 0.52 : random.nextDouble() * 0.6 - 0.3;
            double dy = 0.2 + random.nextDouble() * 9 / 16;
            double dz = axis == Direction.Axis.Z ? facing.getOffsetZ() * 0.52 : random.nextDouble() * 0.6 - 0.3;

            world.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0, 0, 0);
            world.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0, 0, 0);
        }
    }
}
