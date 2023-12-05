package magifacture.block.entity;

import lombok.Getter;
import magifacture.block.MixingColumnBlock;
import magifacture.screen.MixingColumnScreenHandler;
import magifacture.util.FluidTransferUtils;
import magifacture.util.MultipleFluidStorage;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MixingColumnBlockEntity extends MagifactureBlockEntity implements SidedInventory {
    public static final int INVENTORY_SIZE = 6;
    public static final int SLOT_INPUT = 0, //
            SLOT_OUTPUT = 1, //
            SLOT_TO_DRAIN = 2, //
            SLOT_DRAINED = 3, //
            SLOT_TO_FILL = 4, //
            SLOT_FILLED = 5;

    public static final BlockEntityType<MixingColumnBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(MixingColumnBlockEntity::new, MixingColumnBlock.INSTANCE).build();

    @Getter
    protected MixingColumnFluidStorage fluidStorage = new MixingColumnFluidStorage(this);

    protected int extensions = 0;

    public MixingColumnBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public static MixingColumnBlockEntity from(MixingColumnExtensionBlockEntity extension) {
        MixingColumnBlockEntity be = new MixingColumnBlockEntity(extension.getPos(), extension.getCachedState());
        return be;
    }

    public void resetExtensions() {
        this.extensions = 0;
    }

    public void addExtension(MixingColumnExtensionBlockEntity extension) {
        this.extensions++;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.extensions = tag.getInt("ExtensionCount");
        this.fluidStorage.readNbt(tag);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("ExtensionCount", this.extensions);
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

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        for (int i = 0; i < this.extensions; i++) {
            BlockPos pos = this.pos.up(i);
            if (player.squaredDistanceTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) < 64) {
                return true;
            }
        }
        return super.canPlayerUse(player);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case UP -> new int[]{SLOT_INPUT};
            case DOWN -> new int[]{SLOT_OUTPUT, SLOT_DRAINED, SLOT_FILLED};
            default -> new int[]{SLOT_TO_DRAIN, SLOT_TO_FILL, SLOT_DRAINED, SLOT_FILLED};
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return switch (slot) {
            case SLOT_INPUT -> true;
            case SLOT_TO_DRAIN -> FluidTransferUtils.canDrain(stack);
            case SLOT_TO_FILL -> FluidTransferUtils.canFill(stack);
            default -> false;
        };
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public static class MixingColumnFluidStorage extends MultipleFluidStorage {
        protected final MixingColumnBlockEntity be;

        public MixingColumnFluidStorage(MixingColumnBlockEntity be) {
            super();
            this.be = be;
        }

        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            this.be.markDirty();
        }

        @Override
        public long getCapacity() {
            return (this.be.extensions + 1) * 4000L * 81L;
        }

        public void writeNbtWithCapacity(NbtCompound nbt) {
            this.writeNbt(nbt);
            nbt.putLong("Capacity", this.getCapacity());
        }
    }
}
