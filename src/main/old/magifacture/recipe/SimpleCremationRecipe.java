package reoseah.magifacture.recipe;

import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import reoseah.magifacture.block.entity.CrematoriumBlockEntity;

import java.util.Map;

public class SimpleCremationRecipe extends CremationRecipe {
    public static final RecipeSerializer<?> SERIALIZER = new Serializer(200);
    public final Ingredient ingredient;
    public final int count;
    public final ItemStack output;
    public final int duration;
    public final float experience;
    public final boolean scaleOutputByDurability;

    public SimpleCremationRecipe(Identifier id, Ingredient ingredient, int count, ItemStack output, int duration, float experience, boolean scaleOutputByDurability) {
        super(id);
        this.ingredient = ingredient;
        this.count = count;
        this.output = output;
        this.duration = duration;
        this.experience = experience;
        this.scaleOutputByDurability = scaleOutputByDurability;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.ingredient.test(inventory.getStack(0)) && inventory.getStack(0).getCount() >= this.count;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        if (this.scaleOutputByDurability) {
            ItemStack input = inventory.getStack(0);
            if (input.isDamaged()) {
                double durability = 1 - (double) input.getDamage() / (double) input.getMaxDamage();
                int outputCount = Math.min(1, MathHelper.floor(durability * this.output.getCount()));

                ItemStack copy = this.output.copy();
                copy.setCount(outputCount);

                return copy;
            }
        }
        return this.output.copy();
    }

    @Override
    public ItemStack getOutput() {
        return this.output;
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
        protected final int defaultDuration;

        public Serializer(int defaultDuration) {
            this.defaultDuration = defaultDuration;
        }

        @Override
        public SimpleCremationRecipe read(Identifier id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            int count = JsonHelper.hasJsonObject(json, "ingredient") ? JsonHelper.getInt(JsonHelper.getObject(json, "ingredient"), "count", 1) : 1;
            ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"));
            int duration = JsonHelper.getInt(json, "duration", this.defaultDuration);
            float experience = JsonHelper.getFloat(json, "experience", 0F);
            boolean scaleOutputByDurability = JsonHelper.getBoolean(json, "scale_output_by_durability", true);

            return new SimpleCremationRecipe(id, ingredient, count, output, duration, experience, scaleOutputByDurability);
        }

        @Override
        public SimpleCremationRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient ingredient = Ingredient.fromPacket(buf);
            int count = buf.readVarInt();
            ItemStack result = buf.readItemStack();
            int duration = buf.readVarInt();
            float experience = buf.readFloat();
            boolean scaleOutputByDurability = buf.readBoolean();

            return new SimpleCremationRecipe(id, ingredient, count, result, duration, experience, scaleOutputByDurability);
        }

        @Override
        public void write(PacketByteBuf buf, SimpleCremationRecipe recipe) {
            recipe.ingredient.write(buf);
            buf.writeVarInt(recipe.count);
            buf.writeItemStack(recipe.output);
            buf.writeVarInt(recipe.duration);
            buf.writeFloat(recipe.experience);
            buf.writeBoolean(recipe.scaleOutputByDurability);
        }
    }
}
