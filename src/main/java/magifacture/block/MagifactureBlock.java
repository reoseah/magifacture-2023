package magifacture.block;

import magifacture.block.entity.MagifactureBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
 * Contains common features for blocks that have an inventory:
 *   - drop inventory when broken
 *   - copy item name to inventory name if BE extends LockableContainerBlockEntity
 *   - open GUI on right click if BE implements NamedScreenHandlerFactory (override otherwise!)
 *
 * Additional features for blocks from the mod:
 *   - facing the player if block state contains Properties.FACING or HORIZONTAL_FACING
 */
public abstract class MagifactureBlock extends BlockWithEntity {
    protected MagifactureBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomName() && world.getBlockEntity(pos) instanceof LockableContainerBlockEntity nameable) {
            nameable.setCustomName(stack.getName());
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
            if (factory == null) {
                return ActionResult.PASS;
            }
            player.openHandledScreen(factory);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof Inventory inventory) {
                ItemScatterer.spawn(world, pos, inventory);
                world.updateComparators(pos, this);
            }
            if (be instanceof MagifactureBlockEntity entity) {
                entity.onBroken();
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (this.getDefaultState().contains(Properties.HORIZONTAL_FACING)) {
            return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerLookDirection().getOpposite());
        }
        return this.getDefaultState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
        }
        if (state.contains(Properties.FACING)) {
            return state.with(Properties.FACING, rotation.rotate(state.get(Properties.FACING)));
        }
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
        }
        if (state.contains(Properties.FACING)) {
            return state.rotate(mirror.getRotation(state.get(Properties.FACING)));
        }
        return state;
    }
}
