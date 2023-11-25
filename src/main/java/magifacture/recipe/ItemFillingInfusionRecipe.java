package magifacture.recipe;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.Pair;
import magifacture.block.entity.InfuserBlockEntity;
import magifacture.util.FluidTransferHacks;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ItemFillingInfusionRecipe extends InfusionRecipe {
    public static final RecipeSerializer<ItemFillingInfusionRecipe> SERIALIZER = new RecipeSerializer<ItemFillingInfusionRecipe>() {
        private final Codec<ItemFillingInfusionRecipe> codec = Codec.unit(ItemFillingInfusionRecipe::new);

        @Override
        public Codec<ItemFillingInfusionRecipe> codec() {
            return this.codec;
        }

        @Override
        public ItemFillingInfusionRecipe read(PacketByteBuf buf) {
            return new ItemFillingInfusionRecipe();
        }

        @Override
        public void write(PacketByteBuf buf, ItemFillingInfusionRecipe recipe) {

        }
    };

    public ItemFillingInfusionRecipe() {
        super();
    }

    @Override
    public ResourceAmount<FluidVariant> getFluidCost(@Nullable InfuserBlockEntity inventory) {
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
        return new ResourceAmount<>(FluidVariant.of(fluid), FluidTransferHacks.findFilledStack(stack, fluid).value());
    }

    @Override
    public int getDuration(@Nullable InfuserBlockEntity inventory) {
        return 100;
    }

    @Override
    public boolean matches(InfuserBlockEntity inventory, World world) {
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
        Pair<ItemStack, Long> filledStack = FluidTransferHacks.findFilledStack(stack, fluid);
        return filledStack != null
                && filledStack.value() <= inventory.getTank().amount;
    }

    @Override
    public ItemStack craft(InfuserBlockEntity inventory, DynamicRegistryManager registries) {
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
        return FluidTransferHacks.findFilledStack(stack, inventory.getTank().variant.getFluid()).key();
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    @Deprecated
    public ItemStack getResult(DynamicRegistryManager registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
