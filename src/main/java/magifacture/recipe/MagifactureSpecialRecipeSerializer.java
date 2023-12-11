package magifacture.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;

import java.util.function.Supplier;

public class MagifactureSpecialRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Supplier<T> constructor;

    public MagifactureSpecialRecipeSerializer(Supplier<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Codec<T> codec() {
        return Codec.unit(this.constructor);
    }

    @Override
    public T read(PacketByteBuf buf) {
        return this.constructor.get();
    }

    @Override
    public void write(PacketByteBuf buf, T recipe) {
    }
}
