package magifacture.fluid.transfer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import magifacture.item.transfer.InventorySlotStorage;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Optional;

public class FluidTransferUtils {
    public static final Codec<FluidVariant> FLUID_VARIANT_CODEC = RecordCodecBuilder //
            .create(instance -> instance.group(
                    Registries.FLUID.getCodec().fieldOf("fluid").forGetter(FluidVariant::getFluid),
                    NbtCompound.CODEC.optionalFieldOf("nbt").forGetter(variant -> Optional.ofNullable(variant.getNbt()))
            ).apply(instance, (fluid, optionalNbt) -> FluidVariant.of(fluid, optionalNbt.orElse(null))));

    public static final Codec<ResourceAmount<FluidVariant>> FLUID_AMOUNT_CODEC = RecordCodecBuilder //
            .create(instance -> instance.group(
                    FLUID_VARIANT_CODEC.fieldOf("variant").forGetter(ResourceAmount::resource),
                    Codec.LONG.fieldOf("amount").forGetter(ResourceAmount::amount)
            ).apply(instance, ResourceAmount::new));

    public static boolean canFill(ItemStack stack) {
        var ctx = ContainerItemContext.withConstant(stack);
        var storage = ctx.find(FluidStorage.ITEM);
        if (storage != null) {
            return storage.supportsInsertion();
        }
        return false;
    }

    public static boolean canFillWith(ItemStack stack, FluidVariant fluid) {
        var ctx = ContainerItemContext.withConstant(stack);
        var storage = ctx.find(FluidStorage.ITEM);
        if (storage != null) {
            if (fluid == null || fluid.isBlank()) {
                return storage.supportsInsertion();
            }
            return StorageUtil.simulateInsert(storage, fluid, Integer.MAX_VALUE, null) > 0;
        }
        return false;
    }

    public static boolean canDrain(ItemStack stack) {
        var ctx = ContainerItemContext.withConstant(stack);
        var storage = ctx.find(FluidStorage.ITEM);
        if (storage != null) {
            return storage.supportsExtraction();
        }
        return false;
    }

    public static boolean canDrainTo(ItemStack stack, FluidVariant fluid) {
        var ctx = ContainerItemContext.withConstant(stack);
        var storage = ctx.find(FluidStorage.ITEM);
        if (storage != null) {
            return StorageUtil.simulateExtract(storage, fluid, Integer.MAX_VALUE, null) > 0;
        }
        return false;
    }

    public static long tryFillItem(Storage<FluidVariant> storage, Inventory inventory, int inputSlot, int outputSlot, long maxAmount) {
        InventorySlotStorage input = new InventorySlotStorage(inventory, inputSlot) {
            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                if (!canFill(resource.toStack())) {
                    return 0;
                }
                return super.insert(resource, maxAmount, transaction);
            }
        };
        InventorySlotStorage output = new InventorySlotStorage(inventory, outputSlot);
        Storage<FluidVariant> itemStorage = new ContainerItemContext() {
            @Override
            public SingleSlotStorage<ItemVariant> getMainSlot() {
                return input;
            }

            @Override
            public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
                return output.insert(itemVariant, maxAmount, transactionContext);
            }

            @Override
            public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
                return List.of(output);
            }
        }.find(FluidStorage.ITEM);
        if (itemStorage == null) {
            return 0;
        }
        return StorageUtil.move(storage, itemStorage, variant -> true, maxAmount, null);
    }

    public static long tryDrainItem(Storage<FluidVariant> storage, Inventory inventory, int inputSlot, int outputSlot, long maxAmount) {
        InventorySlotStorage input = new InventorySlotStorage(inventory, inputSlot) {
            @Override
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                if (!canDrain(resource.toStack())) {
                    return 0;
                }
                return super.insert(resource, maxAmount, transaction);
            }
        };
        InventorySlotStorage output = new InventorySlotStorage(inventory, outputSlot);
        Storage<FluidVariant> itemStorage = new ContainerItemContext() {
            @Override
            public SingleSlotStorage<ItemVariant> getMainSlot() {
                return input;
            }

            @Override
            public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
                return output.insert(itemVariant, maxAmount, transactionContext);
            }

            @Override
            public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
                return List.of(output);
            }
        }.find(FluidStorage.ITEM);
        if (itemStorage == null) {
            return 0;
        }
        return StorageUtil.move(itemStorage, storage, variant -> true, maxAmount, null);
    }

    public static boolean canFullyInsert(ResourceAmount<FluidVariant> fluid, Storage<FluidVariant> storage) {
        if (fluid == null || fluid.amount() == 0 || fluid.resource().isBlank()) {
            return true;
        }
        return StorageUtil.simulateInsert(storage, fluid.resource(), fluid.amount(), null) == fluid.amount();
    }

    public static long insert(ResourceAmount<FluidVariant> fluid, Storage<FluidVariant> storage) {
        if (fluid == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long change = storage.insert(fluid.resource(), fluid.amount(), transaction);
            transaction.commit();
            return change;
        }
    }
}
