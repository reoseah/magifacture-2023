package magifacture.block.entity.component;

import lombok.Getter;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public abstract class FuelHandler {
    protected final Inventory inventory;
    protected final int fuelSlot;
    @Getter
    protected int fuelLeft;
    @Getter
    protected int fuelDuration;

    public FuelHandler(Inventory inventory, int fuelSlot) {
        this.inventory = inventory;
        this.fuelSlot = fuelSlot;
    }

    public void readNbt(NbtCompound nbt) {
        this.fuelLeft = nbt.getShort("FuelLeft");
        this.fuelDuration = nbt.getShort("FuelDuration");
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putShort("FuelLeft", (short) this.fuelLeft);
        nbt.putShort("FuelDuration", (short) this.fuelDuration);
    }

    protected boolean disallowUsingLava() {
        return false;
    }

    public boolean canConsumeFuel() {
        Item item = this.inventory.getStack(this.fuelSlot).getItem();
        if (item == Items.LAVA_BUCKET && this.disallowUsingLava()) {
            return false;
        }
        return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0) > 0;
    }

    public boolean consumeFuel() {
        ItemStack stack = this.inventory.getStack(this.fuelSlot);
        if (stack.getItem() == Items.LAVA_BUCKET && this.disallowUsingLava()) {
            return false;
        }
        int fuelValue = AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(stack.getItem(), 0);
        if (fuelValue <= 0) {
            return false;
        }
        if (stack.getItem().hasRecipeRemainder()) {
            Item remainder = stack.getItem().getRecipeRemainder();
            if (remainder != null && stack.getCount() == 1) {
                this.inventory.setStack(this.fuelSlot, new ItemStack(remainder));
            }
        }
        stack.decrement(1);

        this.fuelDuration = this.fuelLeft = fuelValue;
        return true;
    }

    public void tick() {
        boolean shouldMarkDirty = false;

        if (this.fuelLeft == 0 && this.canConsumeFuel() && this.canStartBurning()) {
            boolean lit = this.consumeFuel();
            if (lit) {
                this.onBurningStarted();
                shouldMarkDirty = true;
            }
        }

        if (this.fuelLeft > 0) {
            this.onBurningTick();

            if (this.fuelLeft == 1 && !this.canConsumeFuel()) {
                this.onBurningEnded();
            }

            this.fuelLeft--;
            shouldMarkDirty = true;
        } else {
            this.onNonBurningTick();
        }

        if (shouldMarkDirty) {
            this.inventory.markDirty();
        }
    }

    protected abstract boolean canStartBurning();

    protected abstract void onBurningStarted();

    protected abstract void onBurningTick();

    protected abstract void onBurningEnded();

    protected abstract void onNonBurningTick();
}
