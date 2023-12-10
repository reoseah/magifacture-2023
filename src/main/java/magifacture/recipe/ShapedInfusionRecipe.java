package magifacture.recipe;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import magifacture.block.entity.InfuserBlockEntity;
import magifacture.fluid.storage.FluidTransferUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShapedInfusionRecipe extends InfusionRecipe {
    public static final Serializer SERIALIZER = new Serializer();

    @Getter
    private final int width;
    @Getter
    private final int height;
    protected final ResourceAmount<FluidVariant> fluid;
    private final DefaultedList<Ingredient> inputs;
    protected final ItemStack result;
    protected final int duration;

    public ShapedInfusionRecipe(int width, int height, DefaultedList<Ingredient> inputs, ResourceAmount<FluidVariant> fluid, ItemStack result, int duration) {
        this.width = width;
        this.height = height;
        this.fluid = fluid;
        this.inputs = inputs;
        this.result = result;
        this.duration = duration;
    }

    public ResourceAmount<FluidVariant> getFluidCost(InfuserBlockEntity inventory) {
        return inventory != null ? new ResourceAmount<>(inventory.getTank().variant, this.fluid.amount()) : this.fluid;
    }

    @Override
    public int getDuration(@Nullable InfuserBlockEntity inventory) {
        return duration;
    }

    @Override
    public ItemStack craft(InfuserBlockEntity inventory, DynamicRegistryManager registryManager) {
        return this.result.copy();
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean fits(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public boolean matches(InfuserBlockEntity inventory, World world) {
        if (this.fluid.resource().equals(inventory.getTank().variant) //
                && inventory.getTank().amount >= this.fluid.amount()) {
            for (int i = 0; i <= 3 - this.width; ++i) {
                for (int j = 0; j <= 3 - this.height; ++j) {
                    if (this.matchesSmall(inventory, i, j, true) //
                            || this.matchesSmall(inventory, i, j, false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean matchesSmall(InfuserBlockEntity inventory, int offsetX, int offsetY, boolean bl) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int x = i - offsetX;
                int y = j - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
                    if (bl) {
                        ingredient = this.inputs.get(this.width - x - 1 + y * this.width);
                    } else {
                        ingredient = this.inputs.get(x + y * this.width);
                    }
                }

                if (!ingredient.test(inventory.getStack(i + j * 3))) {
                    return false;
                }
            }
        }

        return true;
    }

    public static class Serializer implements RecipeSerializer<ShapedInfusionRecipe> {
        private static final Codec<ShapedInfusionRecipe> CODEC = RawRecipe.CODEC.flatXmap(raw -> {
            String[] pattern = ShapedRecipe.removePadding(raw.pattern);
            int width = pattern[0].length();
            int height = pattern.length;
            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
            Set<String> set = Sets.newHashSet(raw.key.keySet());

            for (int y = 0; y < pattern.length; ++y) {
                String line = pattern[y];

                for (int x = 0; x < line.length(); ++x) {
                    String ch = line.substring(x, x + 1);
                    Ingredient ingredient = ch.equals(" ") ? Ingredient.EMPTY : raw.key.get(ch);
                    if (ingredient == null) {
                        return DataResult.error(() -> "Pattern references symbol '" + ch + "' but it's not defined in the key");
                    }

                    set.remove(ch);
                    ingredients.set(x + width * y, ingredient);
                }
            }

            if (!set.isEmpty()) {
                return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + set);
            }
            ShapedInfusionRecipe recipe = new ShapedInfusionRecipe(width, height, ingredients, raw.fluid, raw.result, raw.duration);
            return DataResult.success(recipe);
        }, recipe -> {
            throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
        });

        @Override
        public Codec<ShapedInfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public ShapedInfusionRecipe read(PacketByteBuf buf) {
            int width = buf.readVarInt();
            int height = buf.readVarInt();
            FluidVariant variant = FluidVariant.fromPacket(buf);
            long amount = buf.readVarLong();
            ResourceAmount<FluidVariant> fluid = new ResourceAmount<>(variant, amount);
            DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromPacket(buf));
            }
            ItemStack result = buf.readItemStack();
            int duration = buf.readVarInt();
            return new ShapedInfusionRecipe(width, height, ingredients, fluid, result, duration);

        }

        @Override
        public void write(PacketByteBuf buf, ShapedInfusionRecipe recipe) {
            buf.writeVarInt(recipe.width);
            buf.writeVarInt(recipe.height);
            recipe.fluid.resource().toPacket(buf);
            buf.writeLong(recipe.fluid.amount());
            for (Ingredient ingredient : recipe.inputs) {
                ingredient.write(buf);
            }
            buf.writeItemStack(recipe.result);
            buf.writeVarInt(recipe.duration);
        }

        public record RawRecipe(Map<String, Ingredient> key, //
                                List<String> pattern, //
                                ResourceAmount<FluidVariant> fluid, //
                                ItemStack result, //
                                int duration) {
            public static final int DEFAULT_DURATION = 200;

            public static final Codec<RawRecipe> CODEC = RecordCodecBuilder.create( //
                    instance -> instance.group( //
                            Codecs.strictUnboundedMap(ShapedRecipe.Serializer.KEY_ENTRY_CODEC, Ingredient.DISALLOW_EMPTY_CODEC).fieldOf("key").forGetter(recipe -> recipe.key), //
                            ShapedRecipe.Serializer.PATTERN_CODEC.fieldOf("pattern").forGetter(recipe -> recipe.pattern), //
                            FluidTransferUtils.FLUID_AMOUNT_CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid), //
                            RecipeCodecs.CRAFTING_RESULT.fieldOf("result").forGetter(recipe -> recipe.result), //
                            Codec.INT.fieldOf("duration").orElse(DEFAULT_DURATION).forGetter(recipe -> recipe.duration) //
                    ).apply(instance, RawRecipe::new));
        }
    }
}