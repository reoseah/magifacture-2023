package magifacture.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;

public class SerializableSingleFluidStorage extends SingleFluidStorage implements NbtSerializable {
    protected final long capacity;

    public SerializableSingleFluidStorage(long capacity) {
        this.capacity = capacity;
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return this.capacity;
    }


    public static SerializableSingleFluidStorage withFixedCapacity(long capacity, Runnable onChange) {
        return new SerializableSingleFluidStorage(capacity) {
            @Override
            protected void onFinalCommit() {
                onChange.run();
            }
        };
    }
}
