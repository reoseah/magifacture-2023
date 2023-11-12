package magifacture.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import magifacture.mixin.AccessibleBucketItem;
import magifacture.mixin.AccessibleEmptyItemFluidStorage;
import magifacture.mixin.AccessibleFullItemFluidStorage;

@SuppressWarnings("UnstableApiUsage")
public class FluidUtils {
    public static ResourceAmount<FluidVariant> fromJson(JsonObject json) {
        if (json == null || json.isJsonNull()) {
            throw new JsonSyntaxException("Fluid cannot be null");
        }
        String string = JsonHelper.getString(json, "fluid");
        Fluid fluid = Registries.FLUID.getOrEmpty(new Identifier(string)).orElseThrow(() -> new JsonSyntaxException("Unknown fluid '" + string + "'"));
        FluidVariant variant = FluidVariant.of(fluid);

        long amount = JsonHelper.getLong(json, "amount") * FluidConstants.BUCKET / 1000;

        return new ResourceAmount<>(variant, amount);
    }

    public static ResourceAmount<FluidVariant> fromPacket(PacketByteBuf buf) {
        FluidVariant variant = FluidVariant.fromPacket(buf);
        long amount = buf.readVarLong();
        return new ResourceAmount<>(variant, amount);
    }

    public static void write(PacketByteBuf buf, ResourceAmount<FluidVariant> fluid) {
        fluid.resource().toPacket(buf);
        buf.writeLong(fluid.amount());
    }

    public static boolean canFillItem(ItemStack stack, Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return stack.getItem() instanceof BucketItem || stack.getItem() == Items.GLASS_BOTTLE;
        }
        return findFilledStack(stack, fluid) != null;
    }

    public static void tryFillItem(SingleFluidStorage tank, Inventory inventory, int emptySlot, int filledSlot) {
        ItemStack emptyStack = inventory.getStack(emptySlot);
        if (emptyStack.isEmpty()) {
            return;
        }

        Pair<ItemStack, Long> stackVolumePair = findFilledStack(emptyStack, tank.getResource().getFluid());
        if (stackVolumePair != null && tank.amount >= stackVolumePair.value()) {
            if (addResultItemToOutputAndRemoveInputItem(inventory, emptySlot, filledSlot, stackVolumePair.key())) {
                tank.amount -= stackVolumePair.value();
                tank.afterOuterClose(TransactionContext.Result.COMMITTED);
            }
        }

        if (tank.amount == 0) {
            tank.variant = FluidVariant.blank();
        }
    }

    public static Pair<ItemStack, Long> findFilledStack(ItemStack emptyStack, Fluid fluid) {
        Item item = emptyStack.getItem();
        if (isEmptyBucket(item)) {
            Item fluidBucket = fluid.getBucketItem();
            if (isValidBucketPair(item, fluidBucket, fluid)) {
                return Pair.of(new ItemStack(fluidBucket), FluidConstants.BUCKET);
            }
        }
        Pair<ItemStack, Long> apiEntry = findFilledStackFromFabricApi(emptyStack, fluid);
        return apiEntry;
    }

    @Nullable
    public static Pair<ItemStack, Long> findFilledStackFromFabricApi(ItemStack emptyStack, Fluid fluid) {
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(emptyStack, ContainerItemContext.withConstant(emptyStack));
        if (storage instanceof CombinedStorage<FluidVariant, ?> combined) {
            for (Storage<FluidVariant> part : combined.parts) {
                if (part instanceof EmptyItemFluidStorage emptyStorage) {
                    AccessibleEmptyItemFluidStorage accessor = (AccessibleEmptyItemFluidStorage) (Object) emptyStorage;
                    if (accessor.getInsertableFluid() == fluid) {
                        ItemStack filledStack = accessor.getEmptyToFullMapping().apply(ItemVariant.of(emptyStack)).toStack();
                        return Pair.of(filledStack, accessor.getInsertableAmount());
                    }
                }
            }
        }
        return null;
    }

    public static boolean isEmptyBucket(Item item) {
        return item instanceof BucketItem bucket && ((AccessibleBucketItem) bucket).getFluid() == Fluids.EMPTY;
    }

    public static boolean isFilledBucket(Item item) {
        return item instanceof BucketItem bucket && ((AccessibleBucketItem) bucket).getFluid() != Fluids.EMPTY;
    }

    public static boolean isValidBucketPair(Item emptyBucket, Item filledBucket, Fluid fluid) {
        return filledBucket.getRecipeRemainder() == emptyBucket && ((AccessibleBucketItem) filledBucket).getFluid() == fluid;
    }

    private static boolean addResultItemToOutputAndRemoveInputItem(Inventory inventory, int inputSlot, int outputSlot, ItemStack outputStack) {
        ItemStack outputStackInSlot = inventory.getStack(outputSlot);
        if (!outputStackInSlot.isEmpty() && !ItemStack.canCombine(outputStackInSlot, outputStack)) {
            return false;
        }
        int maxStackSize = Math.min(outputStackInSlot.getMaxCount(), inventory.getMaxCountPerStack());
        if (!outputStackInSlot.isEmpty() && outputStackInSlot.getCount() >= maxStackSize) {
            return false;
        }
        ItemStack inputStack = inventory.getStack(inputSlot);
        inputStack.decrement(1);
        if (outputStackInSlot.isEmpty()) {
            inventory.setStack(outputSlot, outputStack);
        } else {
            outputStackInSlot.increment(1);
        }
        inventory.markDirty();
        return true;
    }

    public static boolean canDrainItem(ItemStack stack, FluidVariant fluid) {
        Pair<ItemStack, ResourceAmount<FluidVariant>> drainedStack = findDrainedStack(stack);
        return drainedStack != null && (fluid.isBlank() || fluid.equals(drainedStack.value().resource()));
    }

    public static void tryDrainItem(SingleFluidStorage tank, Inventory inventory, int inputSlot, int outputSlot, World world, long maxAmount) {
        ItemStack inputStack = inventory.getStack(inputSlot);
        if (inputStack.isEmpty()) {
            return;
        }
        Pair<ItemStack, ResourceAmount<FluidVariant>> stackVolumePair = findDrainedStack(inputStack);
        if (stackVolumePair != null
                && (tank.isResourceBlank() || tank.variant.equals(stackVolumePair.value().resource()))
                && tank.getAmount() + stackVolumePair.value().amount() <= tank.getCapacity()) {
            if (addResultItemToOutputAndRemoveInputItem(inventory, inputSlot, outputSlot, stackVolumePair.key())) {
                if (tank.isResourceBlank()) {
                    tank.variant = stackVolumePair.value().resource();
                }
                tank.amount += stackVolumePair.value().amount();
                tank.afterOuterClose(TransactionContext.Result.COMMITTED);
            }
        }
    }

    public static Pair<ItemStack, ResourceAmount<FluidVariant>> findDrainedStack(ItemStack stack) {
        if (isFilledBucket(stack.getItem())) {
            ItemStack emptyBucket = stack.getRecipeRemainder();
            Fluid fluid = ((AccessibleBucketItem) stack.getItem()).getFluid();
            if (isValidBucketPair(emptyBucket.getItem(), stack.getItem(), fluid)) {
                return Pair.of(emptyBucket, new ResourceAmount<>(FluidVariant.of(fluid), FluidConstants.BUCKET));
            }
        }
        if (stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.WATER) {
            return Pair.of(new ItemStack(Items.GLASS_BOTTLE), //
                    new ResourceAmount(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE));
        }

        return findDrainedStackFromFabricApi(stack);
    }

    @Nullable
    public static Pair<ItemStack, ResourceAmount<FluidVariant>> findDrainedStackFromFabricApi(ItemStack stack) {
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage instanceof CombinedStorage<FluidVariant, ?> combined) {
            for (Storage<FluidVariant> part : combined.parts) {
                if (part instanceof FullItemFluidStorage fullStorage) {
                    AccessibleFullItemFluidStorage accessor = (AccessibleFullItemFluidStorage) (Object) fullStorage;

                    return Pair.of(accessor.getFullToEmptyMapping().apply(ItemVariant.of(stack)).toStack(), //
                            new ResourceAmount<>(accessor.getContainedFluid(), accessor.getContainedAmount()));
                }
            }
        }
        return null;
    }
}
