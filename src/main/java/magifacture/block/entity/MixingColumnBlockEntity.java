package magifacture.block.entity;

import lombok.Getter;
import magifacture.block.MixingColumnBlock;
import magifacture.screen.MixingColumnScreenHandler;
import magifacture.util.MultipleFluidStorage;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

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
        try (Transaction transaction = Transaction.openOuter()) {
            fluidStorage.insert(FluidVariant.of(Fluids.LAVA), 1000 * 81, transaction);
        }
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new MixingColumnScreenHandler(syncId, this, playerInventory);
    }
}
