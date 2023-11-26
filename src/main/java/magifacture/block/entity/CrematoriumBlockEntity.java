package magifacture.block.entity;

import lombok.Getter;
import magifacture.block.AlembicBlock;
import magifacture.block.CrematoriumBlock;
import magifacture.block.ExperienceBlock;
import magifacture.block.entity.component.FuelHandler;
import magifacture.fluid.ExperienceFluid;
import magifacture.recipe.CremationRecipe;
import magifacture.screen.CrematoriumScreenHandler;
import magifacture.util.FluidTransferHacks;
import magifacture.util.FluidTransferUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
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

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull", "UnstableApiUsage"})
public class CrematoriumBlockEntity extends MagifactureBlockEntity implements SidedInventory {
    public static final int INPUT_SLOT = 0, FUEL_SLOT = 1, OUTPUT_SLOT = 2, EMPTY_SLOT = 3, FILLED_SLOT = 4;

    public static final int[] SLOTS_TOP = {INPUT_SLOT};
    public static final int[] SLOTS_SIDE = {FUEL_SLOT, EMPTY_SLOT};
    public static final int[] SLOTS_BOTTOM = {OUTPUT_SLOT, FILLED_SLOT, FUEL_SLOT};

    public static final BlockEntityType<CrematoriumBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(CrematoriumBlockEntity::new, CrematoriumBlock.INSTANCE).build();
    @Getter
    protected final SingleFluidStorage tank = SingleFluidStorage.withFixedCapacity(4000 * 81, this::markDirty);

    @Getter
    protected int recipeProgress;
    protected @Nullable Optional<? extends CremationRecipe> cachedRecipe;

    protected final FuelHandler fuel = new FuelHandler(this, FUEL_SLOT) {
        @Override
        protected boolean canStartBurning() {
            CremationRecipe recipe = CrematoriumBlockEntity.this.findRecipe(world);
            return recipe != null && CrematoriumBlockEntity.this.canAcceptRecipeOutput(recipe);
        }

        @Override
        protected void onBurningStarted() {
            world.setBlockState(pos, getCachedState().with(Properties.LIT, true));
        }

        @Override
        protected void onBurningTick() {
            CremationRecipe recipe = CrematoriumBlockEntity.this.findRecipe(world);
            if (recipe != null && CrematoriumBlockEntity.this.canAcceptRecipeOutput(recipe)) {
                CrematoriumBlockEntity.this.recipeProgress++;
                if (CrematoriumBlockEntity.this.recipeProgress >= recipe.getDuration()) {
                    CrematoriumBlockEntity.this.craftRecipe(recipe);
                    CrematoriumBlockEntity.this.recipeProgress = 0;
                }
            } else if (CrematoriumBlockEntity.this.recipeProgress > 0) {
                CrematoriumBlockEntity.this.recipeProgress = 0;
            }
        }

        @Override
        protected void onBurningEnded() {
            world.setBlockState(pos, getCachedState().with(Properties.LIT, false));
        }

        @Override
        protected void onNonBurningTick() {
            if (CrematoriumBlockEntity.this.recipeProgress > 0) {
                CrematoriumBlockEntity.this.recipeProgress = Math.max(CrematoriumBlockEntity.this.recipeProgress - 2, 0);
                CrematoriumBlockEntity.this.markDirty();
            }
        }
    };

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
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.tank.readNbt(nbt);
        this.fuel.readNbt(nbt);
        this.recipeProgress = Math.max(nbt.getInt("RecipeProgress"), 0);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.tank.writeNbt(nbt);
        nbt.putInt("RecipeProgress", this.recipeProgress);
        this.fuel.writeNbt(nbt);
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
        FluidTransferUtils.tryFillItem(be.tank, be, EMPTY_SLOT, FILLED_SLOT, Integer.MAX_VALUE);

        be.fuel.tick();
        be.markDirty();
    }

    protected boolean canAcceptRecipeOutput(CremationRecipe recipe) {
        return this.canFullyAddStack(OUTPUT_SLOT, recipe.getResult(this.world.getRegistryManager()))
                && (recipe.getFluid() == null || FluidTransferHacks.canFullyInsert(recipe.getFluid(), this.tank));
    }

    protected void craftRecipe(CremationRecipe recipe) {
        float experience = recipe.getExperience(this);
        BlockPos above = this.pos.up();
        if (this.world.getBlockState(above).isOf(AlembicBlock.INSTANCE)) {
            int experienceMb = MathHelper.floor(experience * ExperienceFluid.MILLIBUCKET_PER_XP * 81);

            BlockEntity be = this.world.getBlockEntity(above);
            if (be instanceof AlembicBlockEntity alembic //
                    && (alembic.tank.variant.getFluid() == ExperienceFluid.INSTANCE || alembic.tank.variant.isBlank())) {
                long change = FluidTransferHacks.insert(new ResourceAmount<>(FluidVariant.of(ExperienceFluid.INSTANCE), experienceMb), alembic.tank);

                if (change < experienceMb) {
                    dropExperience(this.world, above.up(), probabilityRound((experienceMb - change) / (float) ExperienceFluid.MILLIBUCKET_PER_XP / 81F, this.world.random));
                }
            }
        } else {
            dropExperience(this.world, above, probabilityRound(experience, this.world.random));
        }

        this.addStack(OUTPUT_SLOT, recipe.craft(this, this.world.getRegistryManager()));

        if (recipe.getFluid() != null) {
            FluidTransferHacks.insert(recipe.getFluid(), this.tank);
        }
        ItemStack input = this.slots.get(INPUT_SLOT);
        input.decrement(recipe.getInputCount());
        this.setStack(INPUT_SLOT, input); // updates cached recipe
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

    public int getRecipeDuration() {
        return this.cachedRecipe != null && this.cachedRecipe.isPresent() ? this.cachedRecipe.get().getDuration() : 0;
    }

    public int getFuelLeft() {
        return this.fuel.getFuelLeft();
    }

    public int getFuelDuration() {
        return this.fuel.getFuelDuration();
    }
}
