package magifacture.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public abstract class MagifactureBlockEntity extends LockableContainerBlockEntity {
    protected final DefaultedList<ItemStack> slots;

    protected MagifactureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.slots = this.createSlotsList();
    }

    protected abstract DefaultedList<ItemStack> createSlotsList();

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.slots.clear();
        Inventories.readNbt(nbt, this.slots);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.slots);
    }

    // region Inventory
    @Override
    public int size() {
        return this.slots.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.slots) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.slots.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.slots, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.slots, slot);
    }

    @Override
    public void setStack(int slot, ItemStack newStack) {
        this.slots.set(slot, newStack);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean canPlayerUse(PlayerEntity player) {
        return this.world.getBlockEntity(this.pos) == this && player.squaredDistanceTo(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void clear() {
        this.slots.clear();
    }
    // endregion

    /**
     * Common implementation that should work for most of the vanilla-like inventories.
     *
     * @return translation text of "block.[mod_id].[block_entity_name]"
     */
    @Override
    protected Text getContainerName() {
        return Text.translatable(Util.createTranslationKey("block", Registries.BLOCK_ENTITY_TYPE.getId(this.getType())));
    }

    protected boolean canFullyAddStack(int slot, ItemStack offer) {
        ItemStack stackInSlot = this.getStack(slot);
        if (stackInSlot.isEmpty() || offer.isEmpty()) {
            return true;
        }
        return ItemStack.canCombine(stackInSlot, offer) && stackInSlot.getCount() + offer.getCount() <= Math.min(stackInSlot.getMaxCount(), this.getMaxCountPerStack());
    }

    protected void addStack(int slot, ItemStack stack) {
        ItemStack stackInSlot = this.getStack(slot);
        if (stackInSlot.isEmpty()) {
            this.setStack(slot, stack);
        } else if (stackInSlot.getItem() == stack.getItem()) {
            stackInSlot.increment(stack.getCount());
        }
        this.markDirty();
    }

    public void onBroken() {
    }
}
