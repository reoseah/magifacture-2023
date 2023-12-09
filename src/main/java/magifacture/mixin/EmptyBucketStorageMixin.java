package magifacture.mixin;

import magifacture.fluid.MoltenMagicCrystalFluid;
import magifacture.item.MoltenMagicCrystalBucket;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.fluid.EmptyBucketStorage;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmptyBucketStorage.class)
public class EmptyBucketStorageMixin {
    @Shadow
    @Final
    private ContainerItemContext context;

    @Inject(at = @At("HEAD"), method = "insert(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;JLnet/fabricmc/fabric/api/transfer/v1/transaction/TransactionContext;)J", cancellable = true, remap = false)
    public void insert(FluidVariant resource, long maxAmount, TransactionContext transaction, CallbackInfoReturnable<Long> cir) {
        if (context.getItemVariant().isOf(Items.BUCKET) && resource.isOf(MoltenMagicCrystalFluid.Still.INSTANCE)) {
            if (maxAmount >= FluidConstants.BUCKET){
                ItemVariant filledBucket = ItemVariant.of(MoltenMagicCrystalBucket.INSTANCE, resource.hasNbt() ? resource.getNbt().copy() : null);
                if (context.exchange(filledBucket, 1, transaction) == 1) {
                    cir.setReturnValue(FluidConstants.BUCKET);
                }
            } else {
                cir.setReturnValue(0L);
            }
        }
    }
}
