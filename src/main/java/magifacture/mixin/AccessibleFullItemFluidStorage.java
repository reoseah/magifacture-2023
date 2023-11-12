package magifacture.mixin;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(FullItemFluidStorage.class)
public interface AccessibleFullItemFluidStorage {
    @Accessor
    Item getFullItem();

    @Accessor
    Function<ItemVariant, ItemVariant> getFullToEmptyMapping();

    @Accessor
    FluidVariant getContainedFluid();

    @Accessor
    long getContainedAmount();
}
