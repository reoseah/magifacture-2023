package magifacture.mixin;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(EmptyItemFluidStorage.class)
public interface AccessibleEmptyItemFluidStorage {
    @Accessor
    Item getEmptyItem();

    @Accessor
    Function<ItemVariant, ItemVariant> getEmptyToFullMapping();

    @Accessor
    Fluid getInsertableFluid();

    @Accessor
    long getInsertableAmount();
}
