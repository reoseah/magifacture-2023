package magifacture.block;

import magifacture.block.entity.AlembicBlockEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AlembicBlock extends MagifactureBlock {
    public static final VoxelShape SHAPE = VoxelShapes.union(createCuboidShape(0, 0, 0, 16, 13, 16), createCuboidShape(4, 13, 4, 12, 16, 12));

    public static final Block INSTANCE = new AlembicBlock(FabricBlockSettings.create().mapColor(MapColor.GRAY).strength(3F));
    public static final Item ITEM = new BlockItem(INSTANCE, new FabricItemSettings());

    public AlembicBlock(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, AlembicBlockEntity.TYPE, AlembicBlockEntity::tickServer);
    }
}
