package magifacture.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrays;
import magifacture.block.ExperienceBlock;
import magifacture.block.InfuserBlock;
import magifacture.fluid.ExperienceFluid;
import magifacture.recipe.InfusionRecipe;
import magifacture.screen.InfuserScreenHandler;
import magifacture.util.FluidTransferHacks;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull", "UnstableApiUsage"})
public class InfuserBlockEntity extends ProcessingBlockEntity<InfusionRecipe> implements SidedInventory {
    public static final int CAPACITY_MB = 4000;

    public static final int[] INPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int SLOT_OUTPUT = 9, SLOT_FULL = 10, SLOT_DRAINED = 11;
    public static final int INVENTORY_SIZE = 12;

    public static final BlockEntityType<InfuserBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(InfuserBlockEntity::new, InfuserBlock.INSTANCE).build();

    protected final SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(CAPACITY_MB * 81, () -> {
        this.markDirty();
        if (this.cachedRecipe != null) {
            if (this.cachedRecipe.isEmpty()) {
                this.resetCachedRecipe();
            } else {
                InfusionRecipe recipe = this.cachedRecipe.get();
                if (!recipe.matches(this, this.world)) {
                    this.resetCachedRecipe();
                }
            }
        }
    });

    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new InfuserScreenHandler(syncId, this, playerInventory);
    }

    public static void tickServer(World world, BlockPos pos, BlockState state, InfuserBlockEntity be) {
        FluidTransferHacks.tryDrainItem(be.tank, be, SLOT_FULL, SLOT_DRAINED, be.world, CAPACITY_MB - be.tank.getAmount());
        be.tickRecipe();
    }

    @Override
    protected int getInputsCount() {
        return 9;
    }

    @Override
    protected Optional<InfusionRecipe> findRecipeInternal(@NotNull World world) {
        return world.getRecipeManager().getFirstMatch(InfusionRecipe.TYPE, this, world).map(RecipeEntry::value);
    }

    @Override
    protected boolean canCraft(InfusionRecipe recipe) {
        return recipe.matches(this, this.world) && this.canFullyAddStack(SLOT_OUTPUT, recipe.craft(this, this.world.getRegistryManager()));
    }

    @Override
    protected void craftRecipe(InfusionRecipe recipe) {
        this.addStack(SLOT_OUTPUT, recipe.craft(this, this.getWorld().getRegistryManager()));
        this.tank.amount -= recipe.getFluidCost(this).amount();

        if (this.tank.amount == 0) {
            this.tank.variant = FluidVariant.blank();
        }
        for (int i = 0; i < 9; i++) {
            if (!this.getStack(i).isEmpty()) {
                this.getStack(i).decrement(1);
            }
        }
    }

    @Override
    protected int getRecipeDuration(InfusionRecipe recipe) {
        return 100;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.tank.readNbt(tag);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        this.tank.writeNbt(tag);
    }

    public SingleFluidStorage getTank() {
        return this.tank;
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
                && (slot != SLOT_FULL || FluidTransferHacks.canDrainItem(stack, FluidVariant.blank()));
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
}
