package magifacture.recipe;

import magifacture.fluid.MagicCrystalFluid;
import magifacture.fluid.transfer.MultipleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Supplier;

public class MagicCrystalMixingRecipe<T extends Inventory & Supplier<MultipleFluidStorage>> extends MixingRecipe<T> {
    public static final RecipeSerializer<MagicCrystalMixingRecipe<?>> SERIALIZER = new MagifactureSpecialRecipeSerializer<>(MagicCrystalMixingRecipe::new);

    @Override
    public boolean matches(T inventory, World world) {
        MultipleFluidStorage fluids = inventory.get();

        return fluids.getFluidMap().keySet().stream() //
                .filter(variant -> variant.isOf(MagicCrystalFluid.Still.INSTANCE)) //
                .count() >= 2;
    }

    @Override
    public ItemStack craft(T inventory, DynamicRegistryManager registryManager) {
        MultipleFluidStorage fluids = inventory.get();

        Optional<ResourceAmount<FluidVariant>> result = fluids.getFluidMap().object2LongEntrySet().stream() //
                .filter(entry -> entry.getKey().isOf(MagicCrystalFluid.Still.INSTANCE)) //
                .map(entry -> new ResourceAmount<>(entry.getKey(), entry.getLongValue())) //
                .reduce(MagicCrystalFluid::mix);
        if (result.isPresent()) {
            fluids.getFluidMap().keySet().removeIf(variant -> variant.isOf(MagicCrystalFluid.Still.INSTANCE));
            fluids.getFluidMap().put(result.get().resource(), result.get().amount());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
