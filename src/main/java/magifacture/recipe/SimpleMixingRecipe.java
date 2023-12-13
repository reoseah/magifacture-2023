package magifacture.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import magifacture.fluid.transfer.FluidTransferUtils;
import magifacture.fluid.transfer.MultipleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class SimpleMixingRecipe<T extends Inventory & Supplier<MultipleFluidStorage>> extends MixingRecipe<T> {
    public static final RecipeSerializer<SimpleMixingRecipe<?>> SERIALIZER = new Serializer();

    @Getter
    private final Object2LongMap<FluidVariant> input;
    @Getter
    private final ResourceAmount<FluidVariant> result;

    public SimpleMixingRecipe(Object2LongMap<FluidVariant> input, ResourceAmount<FluidVariant> result) {
        this.input = input;
        this.result = result;
    }

    @Override
    public boolean matches(T inventory, World world) {
        return this.input.object2LongEntrySet().stream() //
                .allMatch(entry -> inventory.get().getFluidMap().getLong(entry.getKey()) >= entry.getLongValue());
    }

    @Override
    public ItemStack craft(T inventory, DynamicRegistryManager registryManager) {
        if (!this.matches(inventory, null)) {
            return ItemStack.EMPTY;
        }
        Object2LongMap<FluidVariant> fluids = inventory.get().getFluidMap();
        long amount = fluids.object2LongEntrySet().stream() //
                .filter(entry -> this.input.containsKey(entry.getKey())) //
                .mapToLong(entry -> entry.getLongValue() / this.input.getLong(entry.getKey())) //
                .min() //
                .orElse(0);
        if (amount > 0) {
            fluids.object2LongEntrySet().stream() //
                    .filter(entry -> this.input.containsKey(entry.getKey())) //
                    .forEach(entry -> fluids.put(entry.getKey(), entry.getLongValue() - amount * this.input.getLong(entry.getKey())));
            fluids.put(this.result.resource(), fluids.getLong(this.result.resource()) + amount * this.result.amount());
            fluids.object2LongEntrySet().removeIf(entry -> entry.getLongValue() <= 0);
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

    public static class Serializer implements RecipeSerializer<SimpleMixingRecipe<?>> {
        public static final Codec<ResourceAmount<FluidVariant>> OPTIONAL_AMOUNT_CODEC = RecordCodecBuilder //
                .create(instance -> instance.group( //
                                FluidTransferUtils.FLUID_VARIANT_CODEC.fieldOf("variant").forGetter(ResourceAmount::resource), //
                                Codec.LONG.fieldOf("amount").orElse(1L).forGetter(ResourceAmount::amount)) //
                        .apply(instance, ResourceAmount::new));

        public static final Codec<Object2LongMap<FluidVariant>> FLUID_MAP_CODEC = Codec //
                .list(OPTIONAL_AMOUNT_CODEC) //
                .xmap(list -> {
                    Object2LongMap<FluidVariant> map = new Object2LongLinkedOpenHashMap<>();
                    list.forEach(amount -> map.put(amount.resource(), amount.amount()));
                    return map;
                }, map -> map.object2LongEntrySet().stream() //
                        .map(entry -> new ResourceAmount<>(entry.getKey(), entry.getLongValue())) //
                        .toList());


        private static final Codec<SimpleMixingRecipe<?>> CODEC = RecordCodecBuilder //
                .create(instance -> instance.group(//
                                FLUID_MAP_CODEC.fieldOf("input").forGetter(SimpleMixingRecipe::getInput), //
                                FluidTransferUtils.FLUID_AMOUNT_CODEC.fieldOf("result").forGetter(SimpleMixingRecipe::getResult)) //
                        .apply(instance, SimpleMixingRecipe::new));

        @Override
        public Codec<SimpleMixingRecipe<?>> codec() {
            return CODEC;
        }

        @Override
        public SimpleMixingRecipe<?> read(PacketByteBuf buf) {
            int inputCount = buf.readVarInt();
            Object2LongMap<FluidVariant> input = new Object2LongArrayMap<>(inputCount);
            for (int i = 0; i < inputCount; i++) {
                input.put(FluidVariant.fromNbt(buf.readNbt()), buf.readLong());
            }
            ResourceAmount<FluidVariant> output = new ResourceAmount<>(FluidVariant.fromNbt(buf.readNbt()), buf.readLong());
            return new SimpleMixingRecipe<>(input, output);
        }

        @Override
        public void write(PacketByteBuf buf, SimpleMixingRecipe<?> recipe) {
            buf.writeVarInt(recipe.input.size());
            recipe.input.forEach((fluid, amount) -> {
                buf.writeNbt(fluid.toNbt());
                buf.writeLong(amount);
            });
            buf.writeNbt(recipe.result.resource().toNbt());
        }
    }
}
