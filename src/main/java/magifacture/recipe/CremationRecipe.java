package magifacture.recipe;

import magifacture.block.entity.CrematoriumBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: implement fluid output
 */
public abstract class CremationRecipe implements Recipe<Inventory> {
    public static final RecipeType<CremationRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magifacture:cremation";
        }
    };

    public abstract int getDuration();

    public abstract int getInputCount();

    public abstract float getExperience(@Nullable CrematoriumBlockEntity inventory);

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}