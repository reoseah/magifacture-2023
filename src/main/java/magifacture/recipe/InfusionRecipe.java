package magifacture.recipe;

import magifacture.block.entity.InfuserBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import org.jetbrains.annotations.Nullable;

public abstract class InfusionRecipe implements Recipe<InfuserBlockEntity> {
    public static final RecipeType<InfusionRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magifacture:infusion";
        }
    };

    public abstract ResourceAmount<FluidVariant> getFluidCost(@Nullable InfuserBlockEntity inventory);

    public abstract int getDuration(@Nullable InfuserBlockEntity inventory);

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}