package magifacture.item.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class InventorySlotStorage extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<ItemVariant> {
    protected final Inventory inventory;
    protected final int slot;

    public InventorySlotStorage(Inventory inventory, int slot) {
        this.inventory = inventory;
        this.slot = slot;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack current = this.inventory.getStack(this.slot);
        if (resource.matches(current) || current.isEmpty()) {
            int maxCount = Math.min(current.isEmpty() ? 64 : current.getMaxCount(), this.inventory.getMaxCountPerStack());
            int inserted = Math.min(maxCount - current.getCount(), (int) maxAmount);
            this.updateSnapshots(transaction);
            ItemStack newStack = resource.toStack(current.getCount() + inserted);
            this.inventory.setStack(this.slot, newStack);
            return inserted;
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack current = this.inventory.getStack(this.slot);
        if (resource.matches(current)) {
            int extracted = Math.min(current.getCount(), (int) maxAmount);
            this.updateSnapshots(transaction);
            ItemStack newStack = current.copy();
            newStack.decrement(extracted);
            this.inventory.setStack(this.slot, newStack);
            return extracted;
        }
        return 0;
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.inventory.markDirty();
    }

    @Override
    public boolean isResourceBlank() {
        return this.getResource().isBlank();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(this.inventory.getStack(this.slot));
    }

    @Override
    public long getAmount() {
        return this.inventory.getStack(this.slot).getCount();
    }

    @Override
    public long getCapacity() {
        return this.inventory.getMaxCountPerStack();
    }

    @Override
    protected ItemStack createSnapshot() {
        return this.inventory.getStack(this.slot).copy();
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        this.inventory.setStack(this.slot, snapshot);
    }
}
