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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class MultipleFluidStorage //
        extends SnapshotParticipant<Object2LongMap<FluidVariant>> //
        implements Storage<FluidVariant>, NbtSerializable {
    @Getter
    protected Object2LongMap<FluidVariant> fluids = new Object2LongLinkedOpenHashMap<>();
    @Setter
    @Getter
    private long capacity = 0;

    public MultipleFluidStorage(long capacity) {
        this.capacity = capacity;
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
        long inserted = Math.min(maxAmount, this.capacity - total);
        if (inserted <= 0) {
            return 0;
        }

        long current = this.fluids.getOrDefault(resource, 0);
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
        this.fluids.put(resource, current - extracted);

        return extracted;
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        return this.fluids.object2LongEntrySet() //
                .stream() //
                .map(entry -> (StorageView<FluidVariant>) new StorageView<FluidVariant>() {
                    @Override
                    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                        if (resource.isBlank()) {
                            return 0;
                        }

                        long current = entry.getLongValue();
                        long extracted = Math.min(maxAmount, current);
                        if (extracted <= 0) {
                            return 0;
                        }
                        entry.setValue(current - extracted);

                        return extracted;
                    }

                    @Override
                    public boolean isResourceBlank() {
                        return entry.getKey().isBlank();
                    }

                    @Override
                    public FluidVariant getResource() {
                        return entry.getKey();
                    }

                    @Override
                    public long getAmount() {
                        return entry.getLongValue();
                    }

                    @Override
                    public long getCapacity() {
                        return MultipleFluidStorage.this.capacity;
                    }
                }) //
                .toList() //
                .iterator();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.fluids.clear();
        NbtList fluids = nbt.getList("fluids", 10);
        for (int i = 0; i < fluids.size(); i++) {
            NbtCompound fluid = fluids.getCompound(i);
            this.fluids.put(FluidVariant.fromNbt(fluid.getCompound("fluid")), fluid.getLong("amount"));
        }
    }

    @Override
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
}
