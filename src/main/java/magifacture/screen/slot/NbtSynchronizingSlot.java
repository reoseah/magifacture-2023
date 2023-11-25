package magifacture.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NbtSynchronizingSlot extends Slot {
    public NbtSynchronizingSlot(Supplier<NbtCompound> nbtReader, Consumer<NbtCompound> nbtWriter) {
        super(new Inventory() {
            @Override
            public ItemStack getStack(int slot) {
                ItemStack nbtHolder = new ItemStack(Items.CAKE);
                nbtHolder.setNbt(nbtReader.get());
                return nbtHolder;
            }

            @Override
            public void setStack(int slot, ItemStack stack) {
                nbtWriter.accept(stack.getNbt());
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public ItemStack removeStack(int slot, int amount) {
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeStack(int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public void markDirty() {

            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return false;
            }

            @Override
            public void clear() {

            }
        }, 0, -10000, -10000);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
}
