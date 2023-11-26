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
    public static final int CAPACITY_MB = 4000;

    public static final int[] INPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int SLOT_OUTPUT = 9, SLOT_FULL = 10, SLOT_DRAINED = 11;
    public static final int INVENTORY_SIZE = 12;

    public static final BlockEntityType<InfuserBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(InfuserBlockEntity::new, InfuserBlock.INSTANCE).build();

    public final RecipeHandler<InfusionRecipe> recipeHandler = new RecipeHandler<>(this) {
        @SuppressWarnings("OptionalAssignedToNull")
        @Override
        protected Optional<InfusionRecipe> findRecipe() {
            if (world == null) {
                return null;
            }
            return world.getRecipeManager() //
                    .getFirstMatch(InfusionRecipe.TYPE, InfuserBlockEntity.this, world) //
                    .map(RecipeEntry::value);
        }

        @Override
        public boolean canCraft(InfusionRecipe recipe) {
            return recipe.matches(InfuserBlockEntity.this, world) //
                    && canFullyAddStack(SLOT_OUTPUT, recipe.craft(InfuserBlockEntity.this, world.getRegistryManager()));

        }

        @Override
        protected void craftRecipe(InfusionRecipe recipe) {
            addStack(SLOT_OUTPUT, recipe.craft(InfuserBlockEntity.this, getWorld().getRegistryManager()));
            tank.amount -= recipe.getFluidCost(InfuserBlockEntity.this).amount();

            if (tank.amount == 0) {
                tank.variant = FluidVariant.blank();
            }
            for (int i = 0; i < 9; i++) {
                if (!getStack(i).isEmpty()) {
                    getStack(i).decrement(1);
                }
            }
        }

        @Override
        protected int getRecipeDuration(InfusionRecipe recipe) {
            return recipe.getDuration(InfuserBlockEntity.this);
        }
    };

    @Getter
    protected final SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(CAPACITY_MB * 81, () -> {
        this.markDirty();
        this.recipeHandler.resetCachedRecipe();
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
        FluidTransferUtils.tryDrainItem(be.tank, be, SLOT_FULL, SLOT_DRAINED, CAPACITY_MB - be.tank.getAmount());
        be.recipeHandler.tickRecipe(1);
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
}
