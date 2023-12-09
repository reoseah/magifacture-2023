package magifacture.util;

import magifacture.fluid.MoltenMagicCrystalFluid;
import magifacture.item.MoltenMagicCrystalBucket;
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

public class EmptyBucketMoltenMagicCrystalStorage implements InsertionOnlyStorage<FluidVariant> {
    private final List<StorageView<FluidVariant>> view = List.of(new BlankVariantView<>(FluidVariant.blank(), FluidConstants.BUCKET));
    private final ContainerItemContext context;

    public EmptyBucketMoltenMagicCrystalStorage(ContainerItemContext context) {
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
        if (resource.isOf(MoltenMagicCrystalFluid.Still.INSTANCE) && maxAmount >= FluidConstants.BUCKET) {
            ItemVariant itemVariant = ItemVariant.of(MoltenMagicCrystalBucket.INSTANCE, resource.hasNbt() ? resource.getNbt().copy() : null);
            if (context.exchange(itemVariant, 1, transaction) == 1) {
                return FluidConstants.BUCKET;
            }
        }
        return 0;
    }
}
