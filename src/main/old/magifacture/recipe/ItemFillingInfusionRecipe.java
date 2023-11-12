package reoseah.magifacture.recipe;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import reoseah.magifacture.block.entity.InfusionTableBlockEntity;
import reoseah.magifacture.util.FluidUtils;

public class ItemFillingInfusionRecipe extends InfusionRecipe {
    public static final RecipeSerializer<ItemFillingInfusionRecipe> SERIALIZER = new RecipeSerializer<ItemFillingInfusionRecipe>() {
        @Override
        public ItemFillingInfusionRecipe read(Identifier id, JsonObject json) {
            return new ItemFillingInfusionRecipe(id);
        }

        @Override
        public ItemFillingInfusionRecipe read(Identifier id, PacketByteBuf buf) {
            return new ItemFillingInfusionRecipe(id);
        }

        @Override
        public void write(PacketByteBuf buf, ItemFillingInfusionRecipe recipe) {

        }
    };

    public ItemFillingInfusionRecipe(Identifier id) {
        super(id);
    }

    @Override
    public ResourceAmount<FluidVariant> getFluidCost(@Nullable InfusionTableBlockEntity inventory) {
        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack slot = inventory.getStack(i);
            if (!slot.isEmpty()) {
                if (stack == null) {
                    stack = slot;
                } else {
                    return new ResourceAmount<>(FluidVariant.blank(), 0);
                }
            }
        }
        if (stack == null) {
            return new ResourceAmount<>(FluidVariant.blank(), 0);
        }
        Fluid fluid = inventory.getTank().variant.getFluid();
        return new ResourceAmount<>(FluidVariant.of(fluid), FluidUtils.findFilledStack(stack, fluid).value());
    }

    @Override
    public int getDuration(@Nullable InfusionTableBlockEntity inventory) {
        return 100;
    }

    @Override
    public boolean matches(InfusionTableBlockEntity inventory, World world) {
        if (inventory.getTank().isResourceBlank()) {
            return false;
        }

        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack slot = inventory.getStack(i);
            if (!slot.isEmpty()) {
                if (stack == null) {
                    stack = slot;
                } else {
                    return false;
                }
            }
        }
        if (stack == null) {
            return false;
        }
        Fluid fluid = inventory.getTank().variant.getFluid();
        Pair<ItemStack, Long> filledStack = FluidUtils.findFilledStack(stack, fluid);
        return filledStack != null
                && filledStack.value() <= inventory.getTank().amount;
    }

    @Override
    public ItemStack craft(InfusionTableBlockEntity inventory) {
        ItemStack stack = null;
        for (int i = 0; i < 9; i++) {
            ItemStack slot = inventory.getStack(i);
            if (!slot.isEmpty()) {
                if (stack == null) {
                    stack = slot;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        return FluidUtils.findFilledStack(stack, inventory.getTank().variant.getFluid()).key();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    @Deprecated
    public ItemStack getOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
