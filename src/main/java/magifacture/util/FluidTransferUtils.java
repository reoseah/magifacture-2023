package magifacture.util;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public class FluidTransferUtils {
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
            return StorageUtil.simulateExtract(storage, fluid, Integer.MAX_VALUE, null) > 0;
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
            return StorageUtil.simulateInsert(storage, fluid, Integer.MAX_VALUE, null) > 0;
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
}
