package magifacture.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrays;
import lombok.Getter;
import magifacture.block.ExperienceBlock;
import magifacture.block.InfuserBlock;
import magifacture.block.entity.component.RecipeHandler;
import magifacture.fluid.ExperienceFluid;
import magifacture.recipe.InfusionRecipe;
import magifacture.screen.InfuserScreenHandler;
import magifacture.util.FluidTransferUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class InfuserBlockEntity extends MagifactureBlockEntity implements SidedInventory {
    public static final int FLUID_CAPACITY = 4000 * 81;

    public static final int[] INPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int SLOT_OUTPUT = 9, SLOT_FULL = 10, SLOT_DRAINED = 11;
    public static final int INVENTORY_SIZE = 12;

    public static final BlockEntityType<InfuserBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(InfuserBlockEntity::new, InfuserBlock.INSTANCE).build();

    @Getter
    protected final SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(FLUID_CAPACITY, () -> {
        this.markDirty();
        this.recipeHandler.resetCachedRecipe();
    });
    public final RecipeHandler<InfusionRecipe, InfuserBlockEntity> recipeHandler = new InfuserRecipeHandler(this);

    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.tank.readNbt(nbt);
        this.recipeHandler.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.tank.writeNbt(nbt);
        this.recipeHandler.writeNbt(nbt);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new InfuserScreenHandler(syncId, this, playerInventory);
    }

    @SuppressWarnings("unused")
    public static void tickServer(World world, BlockPos pos, BlockState state, InfuserBlockEntity be) {
        FluidTransferUtils.tryDrainItem(be.tank, be, SLOT_FULL, SLOT_DRAINED, FLUID_CAPACITY - be.tank.getAmount());
        be.recipeHandler.progress();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot < 9) {
            ItemStack previous = this.slots.get(slot);
            boolean needsRecipeUpdate = stack.isEmpty() || !ItemStack.canCombine(previous, stack);

            if (needsRecipeUpdate) {
                this.recipeHandler.resetCachedRecipe();
            }
        }
        this.slots.set(slot, stack);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case UP -> {
                int[] slots = IntArrays.copy(INPUT_SLOTS);
                IntArrays.mergeSort(slots, (a, b) -> Integer.compare(this.getStack(a).getCount(), this.getStack(b).getCount()));
                yield slots;
            }
            case DOWN -> new int[]{SLOT_OUTPUT, SLOT_DRAINED};
            default -> new int[]{SLOT_FULL};
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot != SLOT_OUTPUT && slot != SLOT_DRAINED //
                && (slot != SLOT_FULL || FluidTransferUtils.canDrain(stack));
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void onBroken() {
        if (this.tank.variant.getFluid() == ExperienceFluid.INSTANCE) {
            ExperienceBlock.dropExperience(this.world, this.pos, this.world.random, this.tank.amount / (float) FluidConstants.BUCKET);
        }
    }

    public int getRecipeProgress() {
        return this.recipeHandler.getRecipeProgress();
    }

    public int getRecipeDuration() {
        return this.recipeHandler.getRecipeDuration();
    }

    protected static class InfuserRecipeHandler extends RecipeHandler<InfusionRecipe, InfuserBlockEntity> {
        public InfuserRecipeHandler(InfuserBlockEntity inventory) {
            super(inventory);
        }

        @SuppressWarnings("OptionalAssignedToNull")
        @Override
        protected Optional<InfusionRecipe> findRecipe() {
            if (this.inventory.world == null) {
                return null;
            }
            return this.inventory.world.getRecipeManager() //
                    .getFirstMatch(InfusionRecipe.TYPE, this.inventory, this.inventory.world) //
                    .map(RecipeEntry::value);
        }

        @Override
        public boolean canCraft(InfusionRecipe recipe) {
            return recipe.matches(this.inventory, this.inventory.world) //
                    && this.inventory.canFullyAddStack(SLOT_OUTPUT, recipe.craft(this.inventory, this.inventory.world.getRegistryManager()));
        }

        @Override
        protected void craftRecipe(InfusionRecipe recipe) {
            this.inventory.addStack(SLOT_OUTPUT, recipe.craft(this.inventory, this.inventory.getWorld().getRegistryManager()));
            this.inventory.tank.amount -= recipe.getFluidCost(this.inventory).amount();

            if (this.inventory.tank.amount == 0) {
                this.inventory.tank.variant = FluidVariant.blank();
            }
            for (int i = 0; i < 9; i++) {
                if (!this.inventory.getStack(i).isEmpty()) {
                    this.inventory.getStack(i).decrement(1);
                }
            }
        }

        @Override
        protected int getRecipeDuration(InfusionRecipe recipe) {
            return recipe.getDuration(this.inventory);
        }
    }
}