package magifacture.recipe;

import magifacture.fluid.transfer.MultipleFluidStorage;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

import java.util.function.Supplier;

public abstract class MixingRecipe<T extends Inventory & Supplier<MultipleFluidStorage>> implements Recipe<T> {
    public static final RecipeType<MixingRecipe<?>> TYPE = new RecipeType<>() {
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
