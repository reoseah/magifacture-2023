package magifacture.block.entity;

import lombok.Getter;
import magifacture.block.CrematoriumBlock;
import magifacture.block.entity.component.FuelHandler;
import magifacture.block.entity.component.RecipeHandler;
import magifacture.fluid.ExperienceFluid;
import magifacture.recipe.CremationRecipe;
import magifacture.screen.CrematoriumScreenHandler;
import magifacture.fluid.storage.FluidTransferUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
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

import java.util.Optional;


public class CrematoriumBlockEntity extends MagifactureBlockEntity implements SidedInventory {
    public static final int INPUT_SLOT = 0, FUEL_SLOT = 1, OUTPUT_SLOT = 2, EMPTY_SLOT = 3, FILLED_SLOT = 4;

    public static final BlockEntityType<CrematoriumBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(CrematoriumBlockEntity::new, CrematoriumBlock.INSTANCE).build();

    @Getter
    protected final SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(4000 * 81, this::markDirty);
    protected final RecipeHandler<CremationRecipe, CrematoriumBlockEntity> recipeHandler = new CrematoriumRecipeHandler(this);
    protected final FuelHandler<CrematoriumBlockEntity> fuelHandler = new CrematoriumFuelHandler(this);

    public CrematoriumBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.ofSize(5, ItemStack.EMPTY);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.tank.readNbt(nbt);
        this.recipeHandler.readNbt(nbt);
        this.fuelHandler.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.tank.writeNbt(nbt);
        this.recipeHandler.writeNbt(nbt);
        this.fuelHandler.writeNbt(nbt);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            ItemStack previous = this.slots.get(slot);
            boolean needsRecipeUpdate = stack.isEmpty() || !ItemStack.canCombine(previous, stack);

            if (needsRecipeUpdate) {
                this.recipeHandler.resetCachedRecipe();
            }
        }
        this.slots.set(slot, stack);
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CrematoriumScreenHandler(syncId, this, playerInventory);
    }

    @SuppressWarnings("unused")
    public static void tickServer(World world, BlockPos pos, BlockState state, CrematoriumBlockEntity be) {
        FluidTransferUtils.tryFillItem(be.tank, be, EMPTY_SLOT, FILLED_SLOT, Integer.MAX_VALUE);

        be.fuelHandler.tick();
    }

    // region SidedInventory
    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case UP -> new int[]{INPUT_SLOT};
            case DOWN -> new int[]{OUTPUT_SLOT, FILLED_SLOT, FUEL_SLOT};
            default -> new int[]{FUEL_SLOT, EMPTY_SLOT};
        };
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

    public int getRecipeProgress() {
        return this.recipeHandler.getRecipeProgress();
    }

    public int getRecipeDuration() {
        return this.recipeHandler.getRecipeDuration();
    }

    public int getFuelLeft() {
        return this.fuelHandler.getFuelLeft();
    }

    public int getFuelDuration() {
        return this.fuelHandler.getFuelDuration();
    }

    @SuppressWarnings({"OptionalAssignedToNull", "UnstableApiUsage"})
    private static class CrematoriumRecipeHandler extends RecipeHandler<CremationRecipe, CrematoriumBlockEntity> {
        public CrematoriumRecipeHandler(CrematoriumBlockEntity owner) {
            super(owner);
        }

        @Override
        protected Optional<CremationRecipe> findRecipe() {
            if (this.inventory.world == null) {
                return null;
            }
            return this.inventory.world.getRecipeManager() //
                    .getFirstMatch(CremationRecipe.TYPE, this.inventory, this.inventory.world) //
                    .map(RecipeEntry::value);
        }

        @Override
        public boolean canCraft(CremationRecipe recipe) {
            ItemStack result = recipe.getResult(this.inventory.world.getRegistryManager());
            return this.inventory.canFullyAddStack(OUTPUT_SLOT, result) //
                    && FluidTransferUtils.canFullyInsert(recipe.getFluid(), this.inventory.tank);
        }

        @Override
        protected void craftRecipe(CremationRecipe recipe) {
            float experience = recipe.getExperience(this.inventory);
            this.inventory.addExperienceToAlembicOrDrop(experience);

            ItemStack result = recipe.craft(this.inventory, this.inventory.world.getRegistryManager());
            this.inventory.addStack(OUTPUT_SLOT, result);

            if (recipe.getFluid() != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    this.inventory.tank.insert(recipe.getFluid().resource(), recipe.getFluid().amount(), transaction);
                    transaction.commit();
                }
            }
            ItemStack input = this.inventory.getStack(INPUT_SLOT);
            input.decrement(recipe.getInputCount());
            this.inventory.setStack(INPUT_SLOT, input); // updates cached recipe
        }

        @Override
        protected int getRecipeDuration(CremationRecipe recipe) {
            return recipe.getDuration();
        }
    }

    protected void addExperienceToAlembicOrDrop(float experience) {
        if (this.world.getBlockEntity(this.pos.up()) instanceof AlembicBlockEntity alembic //
                && (alembic.tank.variant.isBlank() || alembic.tank.variant.getFluid() == ExperienceFluid.INSTANCE)) {
            int fluidAmount = MathHelper.floor(experience * ExperienceFluid.MILLIBUCKET_PER_XP * 81);
            long change = FluidTransferUtils.insert(new ResourceAmount<>(FluidVariant.of(ExperienceFluid.INSTANCE), fluidAmount), alembic.tank);

            if (change == fluidAmount) {
                return;
            }
            experience = (fluidAmount - change) / (float) ExperienceFluid.MILLIBUCKET_PER_XP / 81F;
        }
        dropExperience(this.world, this.pos.up(), probabilityRound(experience, this.world.random));
    }

    private static int probabilityRound(float value, Random random) {
        int floor = (int) value;
        return floor + (random.nextFloat() <= value - floor ? 1 : 0);
    }

    public static void dropExperience(World world, BlockPos pos, int xp) {
        while (xp > 0) {
            int orbSize = ExperienceOrbEntity.roundToOrbSize(xp);
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, orbSize));
            xp -= orbSize;
        }
    }

    private static class CrematoriumFuelHandler extends FuelHandler<CrematoriumBlockEntity> {
        public CrematoriumFuelHandler(CrematoriumBlockEntity inventory) {
            super(inventory, CrematoriumBlockEntity.FUEL_SLOT);
        }

        @Override
        protected boolean canStartBurning() {
            return this.inventory.recipeHandler.canCraft();
        }

        @Override
        protected void onBurningStarted() {
            this.inventory.world.setBlockState(this.inventory.pos, //
                    this.inventory.getCachedState().with(Properties.LIT, true));
        }

        @Override
        protected void onBurningTick() {
            this.inventory.recipeHandler.progress();
        }

        @Override
        protected void onBurningEnded() {
            this.inventory.world.setBlockState(this.inventory.pos, //
                    this.inventory.getCachedState().with(Properties.LIT, false));
        }

        @Override
        protected void onNonBurningTick() {
            this.inventory.recipeHandler.regress();
        }
    }
}
