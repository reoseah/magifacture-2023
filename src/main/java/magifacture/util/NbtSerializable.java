package magifacture.util;

import net.minecraft.nbt.NbtCompound;

public interface NbtSerializable {
    void readNbt(NbtCompound nbt);

    void writeNbt(NbtCompound nbt);
}
