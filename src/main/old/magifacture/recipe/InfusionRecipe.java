package reoseah.magifacture.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import reoseah.magifacture.block.InfusionTableBlock;
import reoseah.magifacture.block.entity.InfusionTableBlockEntity;

public abstract class InfusionRecipe implements Recipe<InfusionTableBlockEntity> {
    public static final RecipeType<InfusionRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "magifacture:infusion";
        }
    };

    protected final Identifier id;

    public InfusionRecipe(Identifier id) {
        this.id = id;
    }

    public abstract ResourceAmount<FluidVariant> getFluidCost(@Nullable InfusionTableBlockEntity inventory);

    public abstract int getDuration(@Nullable InfusionTableBlockEntity inventory);

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }



    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack createIcon() {
        return new ItemStack(InfusionTableBlock.ITEM);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getGroup() {
        return "";
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }
}