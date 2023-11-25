package magifacture.screen;

import magifacture.screen.slot.NbtSynchronizingSlot;
import magifacture.util.NbtSerializable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class MagifactureScreenHandler extends ScreenHandler {
    protected final Inventory inventory;
    private int playerFirstSlotIdx = -1;

    protected MagifactureScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, Inventory inventory) {
        super(type, syncId);
        this.inventory = inventory;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    protected Slot addSlot(Slot slot) {
        if (slot.inventory instanceof PlayerInventory && this.playerFirstSlotIdx == -1) {
            this.playerFirstSlotIdx = this.slots.size();
        }
        return super.addSlot(slot);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack previous = stack.copy();
        if (index < this.playerFirstSlotIdx) {
            if (!this.insertItem(stack, this.playerFirstSlotIdx, this.playerFirstSlotIdx + 36, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, previous);
        } else {
            int preferredSlot = this.getPreferredQuickMoveSlot(stack, player.getWorld(), index);

            if (preferredSlot != -1) {
                if (!this.insertItem(stack, preferredSlot, preferredSlot + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < this.playerFirstSlotIdx + 27) {
                if (!this.insertItem(stack, this.playerFirstSlotIdx + 27, this.playerFirstSlotIdx + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.insertItem(stack, this.playerFirstSlotIdx, this.playerFirstSlotIdx + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == previous.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
        return previous;
    }

    protected int getPreferredQuickMoveSlot(ItemStack stack, World world, int slot) {
        return -1;
    }

    protected void addPlayerSlots(PlayerInventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInv, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInv, column, 8 + column * 18, 142));
        }
    }

    protected void addNbtSerializable(NbtSerializable serializable) {
        this.addSlot(new NbtSynchronizingSlot(() -> {
            NbtCompound nbt = new NbtCompound();
            serializable.writeNbt(nbt);
            return nbt;
        }, serializable::readNbt));
    }

    public boolean isInventorySlot(Slot slot) {
        return slot.inventory == this.inventory;
    }
}
