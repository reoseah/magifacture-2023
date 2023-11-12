package reoseah.magifacture.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

public abstract class FueledBlockEntity extends MagifactureBlockEntity {
    protected int fuelLeft;
    protected int fuelDuration;

    protected FueledBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.fuelLeft = nbt.getShort("FuelLeft");
        this.fuelDuration = nbt.getShort("FuelDuration");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("FuelLeft", (short) this.fuelLeft);
        nbt.putShort("FuelDuration", (short) this.fuelDuration);
    }

    protected abstract int getFuelSlot();

    public boolean canConsumeFuel() {
        Item item = this.getStack(this.getFuelSlot()).getItem();
        if (item == Items.LAVA_BUCKET) {
            return false;
        }
        return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0) > 0;
    }

    public void consumeFuel() {
        ItemStack stack = this.getStack(this.getFuelSlot());
        if (stack.getItem() == Items.LAVA_BUCKET) {
            return;
        }
        int fuelValue = AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(stack.getItem(), 0);
        if (fuelValue > 0) {
            if (stack.getItem().hasRecipeRemainder()) {
                Item remainder = stack.getItem().getRecipeRemainder();
                if (stack.getCount() == 1) {
                    this.setStack(this.getFuelSlot(), new ItemStack(remainder));
                } else {
                    Block.dropStack(Objects.requireNonNull(this.world), this.pos, Direction.DOWN, new ItemStack(remainder));
                }
            }
            stack.decrement(1);

            this.fuelDuration = this.fuelLeft = fuelValue;
            this.markDirty();
        }
    }

    @SuppressWarnings("unused")
    public static void tickServer(World world, BlockPos pos, BlockState state, FueledBlockEntity be) {
        boolean wasBurning = state.get(Properties.LIT);

        if (be.fuelLeft > 0) {
            be.fuelLeft--;
            be.markDirty();
        }

        if (be.fuelLeft == 0 && be.canConsumeFuel()) {
            be.consumeFuel();
            be.markDirty();
        }
        if (be.fuelLeft > 0) {
            be.onFuelTick();
        }

        boolean isBurning = be.isActive();
        if (isBurning != wasBurning) {
            world.setBlockState(pos, state.with(Properties.LIT, isBurning));
        }
    }

    protected void onFuelTick() {

    }

    public int getFuelLeft() {
        return fuelLeft;
    }

    public int getFuelDuration() {
        return fuelDuration;
    }

    protected boolean isActive() {
        return this.fuelLeft > 0;
    }
}