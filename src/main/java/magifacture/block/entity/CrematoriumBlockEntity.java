package magifacture.block.entity;

import magifacture.block.AlembicBlock;
import magifacture.block.CrematoriumBlock;
import magifacture.block.ExperienceBlock;
import magifacture.fluid.ExperienceFluid;
import magifacture.recipe.CremationRecipe;
import magifacture.screen.CrematoriumScreenHandler;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class CrematoriumBlockEntity extends FueledBlockEntity implements SidedInventory {
    public static final BlockEntityType<CrematoriumBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(CrematoriumBlockEntity::new, CrematoriumBlock.INSTANCE).build();
    protected final SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(4000 * 81, this::markDirty);

    protected int recipeProgress;
    protected @Nullable Optional<? extends CremationRecipe> cachedRecipe;

    public CrematoriumBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CrematoriumScreenHandler(syncId, this, playerInventory);
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.ofSize(5, ItemStack.EMPTY);
    }

    @Override
    protected int getFuelSlot() {
        return FUEL_SLOT;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.tank.readNbt(nbt);
        this.recipeProgress = Math.max(nbt.getInt("RecipeProgress"), 0);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.tank.writeNbt(nbt);
        nbt.putInt("RecipeProgress", this.recipeProgress);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            ItemStack previous = this.slots.get(slot);
            boolean needsRecipeUpdate = stack.isEmpty() || !ItemStack.canCombine(previous, stack);

            if (needsRecipeUpdate) {
                this.resetCachedRecipe();
            }
        }
        this.slots.set(slot, stack);
    }

    @Override
    public void onBroken() {
        if (this.tank.variant.getFluid() == ExperienceFluid.INSTANCE) {
            ExperienceBlock.dropExperience(this.world, this.pos, this.world.random, this.tank.amount / (float) FluidConstants.BUCKET);
        }
    }

    // region SidedInventory
    public static final int INPUT_SLOT = 0, FUEL_SLOT = 1, OUTPUT_SLOT = 2, EMPTY_SLOT = 3, FILLED_SLOT = 4;

    public static final int[] SLOTS_TOP = {INPUT_SLOT};
    public static final int[] SLOTS_SIDE = {FUEL_SLOT, EMPTY_SLOT};
    public static final int[] SLOTS_BOTTOM = {OUTPUT_SLOT, FILLED_SLOT, FUEL_SLOT};

    @Override
    public int[] getAvailableSlots(Direction side) {
        switch (side) {
            case UP:
                return SLOTS_TOP;
            case DOWN:
                return SLOTS_BOTTOM;
            default:
                return SLOTS_SIDE;
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction side) {
        return switch (slot) {
            case OUTPUT_SLOT, FILLED_SLOT -> false;
            case FUEL_SLOT -> AbstractFurnaceBlockEntity.canUseAsFuel(stack);
            default -> true;
        };
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return switch (slot) {
            case OUTPUT_SLOT, FILLED_SLOT -> true;
            case FUEL_SLOT -> !AbstractFurnaceBlockEntity.canUseAsFuel(stack);
            default -> false;
        };
    }
    //endregion

    protected CremationRecipe findRecipe(World world) {
        if (world == null) {
            return null;
        }
        if (this.cachedRecipe == null && !this.slots.get(0).isEmpty()) {
            Optional<? extends CremationRecipe> recipe = world.getRecipeManager().getFirstMatch(CremationRecipe.TYPE, this, world).map(RecipeEntry::value);
            if (recipe.isPresent()) {
                this.cachedRecipe = recipe;
            }
        }
        return this.cachedRecipe == null ? null : this.cachedRecipe.orElse(null);
    }

    protected void resetCachedRecipe() {
        this.cachedRecipe = null;
        this.recipeProgress = 0;
    }

    public static void tickServer(World world, BlockPos pos, BlockState state, CrematoriumBlockEntity be) {
        boolean wasBurning = state.get(Properties.LIT);

        if (be.fuelLeft > 0) {
            be.fuelLeft--;
            be.markDirty();
        }

        CremationRecipe recipe = be.findRecipe(world);
        if (recipe != null && be.canAcceptRecipeOutput(recipe)) {
            if (be.fuelLeft == 0 && be.canConsumeFuel()) {
                be.consumeFuel();
            }
            if (be.fuelLeft > 0) {
                be.fuelLeft--;
                be.recipeProgress++;
                if (be.recipeProgress >= recipe.getDuration()) {
                    be.craftRecipe(recipe);
                    be.recipeProgress = 0;
                }
            } else {
                be.recipeProgress = Math.max(be.recipeProgress - 2, 0);
            }
            be.markDirty();
        } else if (be.recipeProgress > 0) {
            be.recipeProgress = 0;
            be.markDirty();
        }
        boolean isBurning = be.fuelLeft > 0;
        if (isBurning != wasBurning) {
            world.setBlockState(pos, state.with(Properties.LIT, isBurning));
        }
    }

    protected boolean canAcceptRecipeOutput(CremationRecipe recipe) {
        return this.canFullyAddStack(OUTPUT_SLOT, recipe.getResult(this.world.getRegistryManager()));
    }

    protected void craftRecipe(CremationRecipe recipe) {
        float experience = recipe.getExperience(this);
        BlockPos above = this.pos.up();
        if (this.world.getBlockState(above).isOf(AlembicBlock.INSTANCE)) {
            int amount = MathHelper.floor(experience * ExperienceFluid.MILLIBUCKET_PER_XP * 81);

            BlockEntity be = this.world.getBlockEntity(above);
            if (be instanceof AlembicBlockEntity alembic //
                    && (alembic.tank.variant.getFluid() == ExperienceFluid.INSTANCE || alembic.tank.variant.isBlank())) {
                int change = Math.min(amount, (int) (alembic.tank.getCapacity() - alembic.tank.amount));
                if (change > 0) {
                    if (alembic.tank.variant.isBlank()) {
                        alembic.tank.variant = FluidVariant.of(ExperienceFluid.INSTANCE);
                    }
                    alembic.tank.amount += change;
                    alembic.tank.afterOuterClose(TransactionContext.Result.COMMITTED);
                    amount -= change;
                }
            }

            if (amount > 0) {
                dropExperience(this.world, above.up(), this.world.random, probabilityRound(amount / (float) ExperienceFluid.MILLIBUCKET_PER_XP / 81, this.world.random));
            }
        } else {
            dropExperience(this.world, above, this.world.random, probabilityRound(experience, this.world.random));
        }

        this.addStack(OUTPUT_SLOT, recipe.craft(this, this.world.getRegistryManager()));
        // TODO add fluid to tank

        ItemStack input = this.slots.get(INPUT_SLOT);
        input.decrement(recipe.getInputCount());
        this.setStack(INPUT_SLOT, input); // updates cached recipe
    }

    private static int probabilityRound(float value, Random random) {
        return ((int) value) + random.nextFloat() <= ((value - (int) value)) ? 1 : 0;
    }

    public static void dropExperience(World world, BlockPos pos, Random random, int xp) {
        while (xp > 0) {
            int orbSize = ExperienceOrbEntity.roundToOrbSize(xp);
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, orbSize));
            xp -= orbSize;
        }
    }

    public SingleFluidStorage getTank() {
        return this.tank;
    }

    public int getRecipeProgress() {
        return this.recipeProgress;
    }

    public int getRecipeDuration() {
        return this.cachedRecipe != null && this.cachedRecipe.isPresent() ? this.cachedRecipe.get().getDuration() : 0;
    }
}
