package magifacture.block.entity.component;

import lombok.Getter;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A composable recipe handling for common cases.
 *
 * <p>
 * It caches the recipe, you need to reset it whenever inputs change, like this:
 * <pre>
 *     &#64;Override
 *     public void setStack(int slot, ItemStack stack) {
 *         if (slot < INPUTS_COUNT) {
 *             ItemStack previous = this.slots.get(slot);
 *             boolean needsRecipeUpdate = stack.isEmpty() || !ItemStack.canCombine(previous, stack);
 *
 *             if (needsRecipeUpdate) {
 *                 this.resetCachedRecipe();
 *             }
 *         }
 *         this.slots.set(slot, stack);
 *     }
 * </pre>
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull", "UnstableApiUsage"})
public abstract class RecipeHandler<R extends Recipe<?>, I extends Inventory> {
    protected final I inventory;
    /**
     * Cached recipe, or null if no recipe is cached.
     * If the optional is empty, it means there is no matching recipe.
     */
    protected @Nullable Optional<R> cachedRecipe = null;

    @Getter
    protected int recipeProgress;

    protected RecipeHandler(I inventory) {
        this.inventory = inventory;
    }

    public void readNbt(NbtCompound nbt) {
        this.recipeProgress = Math.max(nbt.getInt("RecipeProgress"), 0);
    }

    public void writeNbt(NbtCompound tag) {
        tag.putInt("RecipeProgress", this.recipeProgress);
    }

    protected abstract @Nullable Optional<R> findRecipe();

    protected abstract boolean canCraft(R recipe);

    protected abstract void craftRecipe(R recipe);

    protected abstract int getRecipeDuration(R recipe);

    public void resetCachedRecipe() {
        this.cachedRecipe = null;
        this.recipeProgress = 0;
    }

    public R getRecipe() {
        if (this.cachedRecipe != null) {
            return this.cachedRecipe.orElse(null);
        }

        this.cachedRecipe = this.findRecipe();
        return this.cachedRecipe == null ? null : this.cachedRecipe.orElse(null);
    }

    public int getRecipeDuration() {
        R recipe = this.getRecipe();
        return recipe != null ? this.getRecipeDuration(recipe) : 0;
    }

    public boolean canCraft() {
        R recipe = this.getRecipe();
        return recipe != null && this.canCraft(recipe);
    }

    public void progress() {
        boolean shouldMarkDirty = false;

        R recipe = this.getRecipe();

        if (recipe != null && this.canCraft(recipe)) {
            int progress = this.getAmountToProgress();
            if (progress > 0) {
                int amount = Math.min(progress, this.getRecipeDuration(recipe) - this.recipeProgress);

                this.recipeProgress += amount;
                this.onRecipeProgress(recipe, amount);

                if (this.recipeProgress >= this.getRecipeDuration(recipe)) {
                    this.craftRecipe(recipe);
                    this.recipeProgress = 0;
                }
            }
            shouldMarkDirty = true;
        } else if (this.recipeProgress > 0) {
            this.recipeProgress = 0;
            shouldMarkDirty = true;
        }

        if (shouldMarkDirty) {
            this.inventory.markDirty();
        }
    }

    protected int getAmountToProgress() {
        return 1;
    }

    protected void onRecipeProgress(R recipe, int amount) {
    }

    public void regress() {
        R recipe = this.getRecipe();

        if (recipe != null && this.canCraft(recipe)) {
            if (this.recipeProgress > 0) {
                int amount = Math.min(this.recipeProgress, this.getAmountToRegress());
                this.recipeProgress -= amount;
                this.onRecipeRegress(recipe, amount);
            }
        }
    }

    protected int getAmountToRegress() {
        return -2;
    }

    protected void onRecipeRegress(R recipe, int amount) {
    }
}