package magifacture.block;

import magifacture.block.entity.MixingColumnBlockEntity;
import magifacture.block.entity.MixingColumnExtensionBlockEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class MixingColumnBlock extends MagifactureBlock {
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;

    public static final Block INSTANCE = new MixingColumnBlock(FabricBlockSettings.create().mapColor(MapColor.GRAY).strength(3F));
    public static final Item ITEM = new BlockItem(INSTANCE, new FabricItemSettings().rarity(Rarity.UNCOMMON));

    protected MixingColumnBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(UP, false).with(DOWN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState() //
                .with(UP, ctx.getWorld().getBlockState(ctx.getBlockPos().up()).getBlock() instanceof MixingColumnBlock)//
                .with(DOWN, ctx.getWorld().getBlockState(ctx.getBlockPos().down()).getBlock() instanceof MixingColumnBlock);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            return state.with(UP, neighborState.getBlock() instanceof MixingColumnBlock);
        } else if (direction == Direction.DOWN) {
            return state.with(DOWN, neighborState.getBlock() instanceof MixingColumnBlock);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        updateBlockEntities(state, world, pos);
        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        updateBlockEntities(state, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private static void updateBlockEntities(BlockState state, World world, BlockPos pos) {
        BlockPos lowest = pos;
        while (world.getBlockState(lowest.down()).isOf(MixingColumnBlock.INSTANCE)) {
            lowest = lowest.down();
        }
        MixingColumnBlockEntity main = null;
        BlockEntity be = world.getBlockEntity(lowest);
        if (be instanceof MixingColumnBlockEntity oldMain) {
            main = oldMain;
        } else if (be instanceof MixingColumnExtensionBlockEntity invalidExtension) {
            MixingColumnBlockEntity newMain = MixingColumnBlockEntity.from(invalidExtension);
            world.removeBlockEntity(lowest);
            world.addBlockEntity(newMain);
            main = newMain;
        }

        main.resetExtensions();
        BlockPos above = lowest;
        while (world.getBlockState(above.up()).isOf(MixingColumnBlock.INSTANCE)) {
            above = above.up();
            BlockEntity be2 = world.getBlockEntity(above);
            if (be2 instanceof MixingColumnExtensionBlockEntity extension) {
                main.addExtension(extension);
                extension.setMainPos(lowest);
            } else if (be2 instanceof MixingColumnBlockEntity invalidMain) {
                MixingColumnExtensionBlockEntity extension = MixingColumnExtensionBlockEntity.from(invalidMain, main);
                world.removeBlockEntity(above);
                world.addBlockEntity(extension);
                main.addExtension(extension);
                extension.setMainPos(lowest);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return !state.get(DOWN) //
                ? new MixingColumnBlockEntity(pos, state) //
                : new MixingColumnExtensionBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : BlockWithEntity.validateTicker(type, MixingColumnBlockEntity.TYPE, MixingColumnBlockEntity::tickServer);
    }
}
