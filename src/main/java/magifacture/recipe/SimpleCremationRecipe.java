package magifacture.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import magifacture.block.entity.CrematoriumBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SimpleCremationRecipe extends CremationRecipe {
    public static final RecipeSerializer<?> SERIALIZER = new Serializer(200);
    public final Ingredient ingredient;
    public final int count;
    public final ItemStack result;
    public final int duration;
    public final float experience;
    public final boolean scaleOutputByDurability = true;

    public SimpleCremationRecipe(Ingredient ingredient, int count, ItemStack result, int duration, float experience) {
        super();
        this.ingredient = ingredient;
        this.count = count;
        this.result = result;
        this.duration = duration;
        this.experience = experience;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.ingredient.test(inventory.getStack(0)) && inventory.getStack(0).getCount() >= this.count;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        if (this.scaleOutputByDurability) {
            ItemStack input = inventory.getStack(0);
            if (input.isDamaged()) {
                double durability = 1 - (double) input.getDamage() / (double) input.getMaxDamage();
                int outputCount = Math.max(1, MathHelper.floor(durability * this.result.getCount()));

                ItemStack copy = this.result.copy();
                copy.setCount(outputCount);

                return copy;
            }
        }
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
    public int getInputCount() {
        return this.count;
    }

    @Override
    public float getExperience(@Nullable CrematoriumBlockEntity inventory) {
        if (inventory != null) {
            return this.experience + this.getExperience(inventory.getStack(0));
        }
        return this.experience;
    }

    public int getExperience(ItemStack stack) {
        int experience = 0;
        Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            experience += entry.getKey().getMinPower(entry.getValue());
        }
        return experience;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    public static class Serializer implements RecipeSerializer<SimpleCremationRecipe> {
        private final Codec<SimpleCremationRecipe> codec;

        public Serializer(int defaultDuration) {
            this.codec = RecordCodecBuilder.create(instance -> instance.group(
                    Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
                    Codec.INT.fieldOf("count").orElse(1).forGetter(recipe -> recipe.count),
                    RecipeCodecs.CRAFTING_RESULT.fieldOf("result").forGetter(recipe -> recipe.result),
                    Codec.INT.fieldOf("duration").orElse(defaultDuration).forGetter(recipe -> recipe.duration),
                    Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(recipe -> recipe.experience)
            ).apply(instance, SimpleCremationRecipe::new));
        }

        @Override
        public Codec<SimpleCremationRecipe> codec() {
            return this.codec;
        }

        @Override
        public SimpleCremationRecipe read(PacketByteBuf buf) {
            Ingredient ingredient = Ingredient.fromPacket(buf);
            int count = buf.readVarInt();
            ItemStack result = buf.readItemStack();
            int duration = buf.readVarInt();
            float experience = buf.readFloat();

            return new SimpleCremationRecipe(ingredient, count, result, duration, experience);
        }

        @Override
        public void write(PacketByteBuf buf, SimpleCremationRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeVarInt(recipe.count);
            buf.writeItemStack(recipe.result);
            buf.writeVarInt(recipe.duration);
            buf.writeFloat(recipe.experience);
        }
    }
}
