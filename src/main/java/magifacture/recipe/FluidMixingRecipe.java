package magifacture.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

public abstract class FluidMixingRecipe implements Recipe<Inventory> {
    public static final RecipeType<FluidMixingRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magifacture:fluid_mixing";
        }
    };

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}
