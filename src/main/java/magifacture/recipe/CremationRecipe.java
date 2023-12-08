package magifacture.recipe;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import org.jetbrains.annotations.Nullable;

public abstract class CremationRecipe implements Recipe<Inventory> {
    public static final RecipeType<CremationRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magifacture:cremation";
        }
    };

    public abstract int getDuration();

    public abstract int getInputCount();

    public abstract float getExperience(@Nullable Inventory inventory);

    public abstract @Nullable ResourceAmount<FluidVariant> getFluid();

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
