package magifacture.util;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public abstract class MultipleFluidStorage //
        extends SnapshotParticipant<Object2LongMap<FluidVariant>> //
        implements Storage<FluidVariant> {
    @Getter
    protected Object2LongMap<FluidVariant> fluids = new Object2LongLinkedOpenHashMap<>();

    public MultipleFluidStorage() {
    }

    public abstract long getCapacity();

    public static MultipleFluidStorage withFixedCapacity(long capacity) {
        return new MultipleFluidStorage() {
            @Override
            public long getCapacity() {
                return capacity;
            }
        };
    }

    @Override
    protected Object2LongMap<FluidVariant> createSnapshot() {
        return new Object2LongLinkedOpenHashMap<>(this.fluids);
    }

    @Override
    protected void readSnapshot(Object2LongMap<FluidVariant> snapshot) {
        this.fluids = snapshot;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank()) {
            return 0;
        }

        long total = this.fluids.values().longStream().sum();
        long inserted = Math.min(maxAmount, this.getCapacity() - total);
        if (inserted <= 0) {
            return 0;
        }

        long current = this.fluids.getOrDefault(resource, 0);
        this.updateSnapshots(transaction);
        this.fluids.put(resource, current + inserted);

        return inserted;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank()) {
            return 0;
        }

        long current = this.fluids.getOrDefault(resource, 0);
        long extracted = Math.min(maxAmount, current);
        if (extracted <= 0) {
            return 0;
        }
        this.updateSnapshots(transaction);
        this.fluids.put(resource, current - extracted);

        return extracted;
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return this.fluids.keySet() //
                .stream() //
                .map(variant -> (StorageView<FluidVariant>) new StorageView<FluidVariant>() {
                    @Override
                    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                        if (resource.isBlank() || !resource.equals(variant)) {
                            return 0;
                        }

                        long current = fluids.getLong(variant);
                        long extracted = Math.min(maxAmount, current);
                        if (extracted <= 0) {
                            return 0;
                        }
                        MultipleFluidStorage.this.updateSnapshots(transaction);
                        fluids.put(variant, current - extracted);

                        return extracted;
                    }

                    @Override
                    public boolean isResourceBlank() {
                        return variant.isBlank();
                    }

                    @Override
                    public FluidVariant getResource() {
                        return variant;
                    }

                    @Override
                    public long getAmount() {
                        return fluids.getLong(variant);
                    }

                    @Override
                    public long getCapacity() {
                        return MultipleFluidStorage.this.getCapacity();
                    }
                }).iterator();
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        this.fluids.object2LongEntrySet().removeIf(entry -> entry.getLongValue() <= 0);
    }

    public void readNbt(NbtCompound nbt) {
        this.fluids.clear();
        NbtList fluids = nbt.getList("fluids", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < fluids.size(); i++) {
            NbtCompound fluid = fluids.getCompound(i);
            this.fluids.put(FluidVariant.fromNbt(fluid.getCompound("fluid")), fluid.getLong("amount"));
        }
    }

    public void writeNbt(NbtCompound nbt) {
        NbtList fluids = new NbtList();
        for (Object2LongMap.Entry<FluidVariant> entry : this.fluids.object2LongEntrySet()) {
            NbtCompound fluid = new NbtCompound();
            fluid.put("fluid", entry.getKey().toNbt());
            fluid.putLong("amount", entry.getLongValue());
            fluids.add(fluid);
        }
        nbt.put("fluids", fluids);
    }

    public static MultipleFluidStorage.WithMutableCapacity withMutableCapacity(long initialCapacity) {
        return new WithMutableCapacity(initialCapacity);
    }

    public static class WithMutableCapacity extends MultipleFluidStorage {
        @Getter
        @Setter
        private long capacity;

        public WithMutableCapacity(long initialCapacity) {
            this.capacity = initialCapacity;
        }

        @Override
        public void readNbt(NbtCompound nbt) {
            super.readNbt(nbt);
            this.capacity = nbt.getLong("Capacity");
        }

        @Override
        public void writeNbt(NbtCompound nbt) {
            super.writeNbt(nbt);
            nbt.putLong("Capacity", this.capacity);
        }
    }
}
