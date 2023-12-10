package magifacture.block.entity;

import magifacture.block.MixingColumnBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class MixingColumnExtensionBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    public static final BlockEntityType<MixingColumnExtensionBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(MixingColumnExtensionBlockEntity::new, MixingColumnBlock.INSTANCE).build();

    protected BlockPos mainPos;

    public MixingColumnExtensionBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public static MixingColumnExtensionBlockEntity from(MixingColumnBlockEntity previous, MixingColumnBlockEntity newMain) {
        previous.fluidStorage.getFluidMap().object2LongEntrySet().forEach(entry -> {
            long sum = entry.getLongValue() + newMain.fluidStorage.getFluidMap().getOrDefault(entry.getKey(), 0);
            newMain.fluidStorage.getFluidMap().put(entry.getKey(), sum);
        });

        MixingColumnExtensionBlockEntity be = new MixingColumnExtensionBlockEntity(previous.getPos(), previous.getCachedState());
        return be;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("MainPos")) {
            this.mainPos = NbtHelper.toBlockPos(nbt.getCompound("MainPos"));
        } else {
            this.mainPos = null;
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.mainPos != null) {
            nbt.put("MainPos", NbtHelper.fromBlockPos(this.mainPos));
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.magifacture.mixing_column");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.createScreenHandler(syncId, playerInventory);
            }
        }
        return null;
    }

    @Override
    public int size() {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.size();
            }
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.isEmpty();
            }
        }
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.getStack(slot);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.removeStack(slot, amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.removeStack(slot);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                main.setStack(slot, stack);
            }
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.canPlayerUse(player);
            }
        }
        return false;
    }

    @Override
    public void clear() {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                main.clear();
            }
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.getAvailableSlots(side);
            }
        }
        return new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.canInsert(slot, stack, dir);
            }
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (this.mainPos != null) {
            BlockEntity be = this.getWorld().getBlockEntity(this.mainPos);
            if (be instanceof MixingColumnBlockEntity main) {
                return main.canExtract(slot, stack, dir);
            }
        }
        return false;
    }

    public void setMainPos(BlockPos lowest) {
        this.mainPos = lowest;
    }
}
