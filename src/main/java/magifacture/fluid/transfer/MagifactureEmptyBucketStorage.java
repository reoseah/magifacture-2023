package magifacture.fluid.transfer;

import magifacture.fluid.MagicCrystalFluid;
import magifacture.item.MagicCrystalBucketItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class MagifactureEmptyBucketStorage implements InsertionOnlyStorage<FluidVariant> {
    private final List<StorageView<FluidVariant>> view = List.of(new BlankVariantView<>(FluidVariant.blank(), FluidConstants.BUCKET));
    private final ContainerItemContext context;

    public MagifactureEmptyBucketStorage(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return this.view.iterator();
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!this.context.getItemVariant().isOf(Items.BUCKET)) {
            return 0;
        }
        if (resource.isOf(MagicCrystalFluid.Still.INSTANCE) && maxAmount >= FluidConstants.BUCKET) {
            ItemVariant full = ItemVariant.of(MagicCrystalBucketItem.INSTANCE, resource.hasNbt() ? resource.getNbt().copy() : null);
            if (context.exchange(full, 1, transaction) == 1) {
                return FluidConstants.BUCKET;
            }
        }
        return 0;
    }
}
