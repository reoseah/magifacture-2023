package reoseah.magifacture.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class ProcessingBlockEntity<R extends Recipe<?>> extends MagifactureBlockEntity {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected @Nullable Optional<R> cachedRecipe;
    protected int recipeProgress;

    protected ProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Return number of inputs slot. The inputs slots should start at 0 and end at this.
     */
    protected abstract int getInputsCount();

    /**
     * Search a matching recipe. Return will be cached until a change in inputs.
     */
    protected abstract Optional<R> findRecipeInternal(@NotNull World world);

    protected abstract int getRecipeDuration(R recipe);

    protected int getProgressPerTick(R recipe) {
        return 1;
    }

    protected void onRecipeProgress(R recipe, int amount) {

    }

    @SuppressWarnings("unchecked")
    protected boolean canCraft(R recipe) {
        return ((Recipe<? super ProcessingBlockEntity<?>>) recipe).matches(this, this.world);
    }

    protected abstract void craftRecipe(R recipe);

    @SuppressWarnings("OptionalAssignedToNull")
    protected void resetCachedRecipe() {
        this.cachedRecipe = null;
        this.recipeProgress = 0;
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public R getRecipe(World world) {
        if (this.cachedRecipe != null) {
            return this.cachedRecipe.orElse(null);
        }
        if (world == null) {
            return null;
        }

        // small optimization to not search recipe for an empty machine
        boolean hasInput = false;
        for (int i = 0; i < this.getInputsCount(); i++) {
            if (!this.getStack(i).isEmpty()) {
                hasInput = true;
                break;
            }
        }
        if (hasInput) {
            this.cachedRecipe = this.findRecipeInternal(world);
        }
        return this.cachedRecipe == null ? null : this.cachedRecipe.orElse(null);
    }

    public int getRecipeDuration() {
        return this.cachedRecipe != null && this.cachedRecipe.isPresent() ? this.getRecipeDuration(this.cachedRecipe.orElse(null)) : 0;
    }

    public int getRecipeProgress() {
        return this.recipeProgress;
    }

    public void tickRecipe() {
//        boolean wasActive = this.getCachedState().get(Properties.LIT);
//        boolean active = false;

        R recipe = this.getRecipe(this.world);

        if (recipe != null && this.canCraft(recipe)) {
            int progress = Math.min(this.getProgressPerTick(recipe), this.getRecipeDuration(recipe) - this.recipeProgress);
            if (progress > 0) {
                this.recipeProgress += progress;
                this.onRecipeProgress(recipe, progress);
//                active = true;

                if (this.recipeProgress >= this.getRecipeDuration(recipe)) {
                    this.craftRecipe(recipe);
                    this.recipeProgress = 0;
                }
            } else {
                this.recipeProgress = Math.max(this.recipeProgress - 2, 0);
            }
            this.markDirty();
        } else if (this.recipeProgress > 0) {
            this.recipeProgress = 0;
            this.markDirty();
        }
//        if (wasActive != active) {
//            this.world.setBlockState(pos, this.getCachedState().with(Properties.LIT, active), 3);
//        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.recipeProgress = Math.max(nbt.getInt("RecipeProgress"), 0);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt("RecipeProgress", this.recipeProgress);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot < this.getInputsCount();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot < this.getInputsCount() && shouldUpdateRecipe(stack, this.slots.get(slot))) {
            this.resetCachedRecipe();
        }
        this.slots.set(slot, stack);
    }

    private static boolean shouldUpdateRecipe(ItemStack stack, ItemStack previous) {
        return stack.isEmpty() || !stack.isItemEqual(previous) || !ItemStack.areNbtEqual(stack, previous);
    }
}
