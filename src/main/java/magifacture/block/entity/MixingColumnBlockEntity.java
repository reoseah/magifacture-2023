package magifacture.block.entity;

import lombok.Getter;
import magifacture.block.MixingColumnBlock;
import magifacture.screen.MixingColumnScreenHandler;
import magifacture.util.FluidTransferUtils;
import magifacture.util.MultipleFluidStorage;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MixingColumnBlockEntity extends MagifactureBlockEntity {
    public static final int INVENTORY_SIZE = 6;
    public static final int SLOT_INPUT = 0, //
            SLOT_OUTPUT = 1, //
            SLOT_TO_DRAIN = 2, //
            SLOT_DRAINED = 3, //
            SLOT_TO_FILL = 4, //
            SLOT_FILLED = 5;

    public static final BlockEntityType<MixingColumnBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(MixingColumnBlockEntity::new, MixingColumnBlock.INSTANCE).build();

    @Getter
    protected MultipleFluidStorage fluidStorage = new MultipleFluidStorage(4000 * 81) {
        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            MixingColumnBlockEntity.this.markDirty();
        }
    };

    public MixingColumnBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.fluidStorage.readNbt(tag);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        this.fluidStorage.writeNbt(tag);
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new MixingColumnScreenHandler(syncId, this, playerInventory);
    }

    public static void tickServer(World world, BlockPos pos, BlockState state, MixingColumnBlockEntity be) {
        FluidTransferUtils.tryDrainItem(be.fluidStorage, be, SLOT_TO_DRAIN, SLOT_DRAINED, Integer.MAX_VALUE);
        FluidTransferUtils.tryFillItem(be.fluidStorage, be, SLOT_TO_FILL, SLOT_FILLED, Integer.MAX_VALUE);
    }
}
