package magifacture.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FluidTransferHacks {
    public static final Codec<FluidVariant> FLUID_VARIANT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.FLUID.getCodec().fieldOf("fluid").forGetter(FluidVariant::getFluid),
            NbtCompound.CODEC.optionalFieldOf("nbt").forGetter(variant -> Optional.ofNullable(variant.getNbt()))
    ).apply(instance, (fluid, optionalNbt) -> FluidVariant.of(fluid, optionalNbt.orElse(null))));

    public static final Codec<ResourceAmount<FluidVariant>> FLUID_AMOUNT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FLUID_VARIANT_CODEC.fieldOf("variant").forGetter(ResourceAmount::resource),
            Codec.LONG.fieldOf("amount").forGetter(ResourceAmount::amount)
    ).apply(instance, ResourceAmount::new));

    public static boolean canFullyInsert(ResourceAmount<FluidVariant> fluid, SingleFluidStorage storage) {
        if (fluid.amount() == 0 || fluid.resource().isBlank()) {
            return true;
        }
        if (storage.variant.isBlank() || storage.variant.equals(fluid.resource())) {
            long insertable = storage.getCapacity() - storage.amount;
            return insertable >= fluid.amount();
        }
        return false;
    }

    public static long insert(ResourceAmount<FluidVariant> fluid, SingleFluidStorage storage) {
        if (storage.variant.isBlank() || storage.variant.equals(fluid.resource())) {
            long change = Math.min(fluid.amount(), storage.getCapacity() - storage.amount);
            if (change > 0) {
                if (storage.variant.isBlank()) {
                    storage.variant = fluid.resource();
                }
                storage.amount += change;
                storage.afterOuterClose(TransactionContext.Result.COMMITTED);
                return change;
            }
        }
        return 0;
    }
}
