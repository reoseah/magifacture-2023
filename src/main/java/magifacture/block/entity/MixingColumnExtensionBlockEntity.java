package magifacture.block.entity;

import magifacture.block.MixingColumnBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class MixingColumnExtensionBlockEntity extends MagifactureBlockEntity {
    public static final BlockEntityType<MixingColumnExtensionBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(MixingColumnExtensionBlockEntity::new, MixingColumnBlock.INSTANCE).build();

    protected BlockPos mainPos;

    public MixingColumnExtensionBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("MainPos")) {
            this.mainPos = NbtHelper.toBlockPos(nbt.getCompound("MainPos"));
        } else {
            this.mainPos = null;
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.mainPos != null) {
            nbt.put("MainPos", NbtHelper.fromBlockPos(this.mainPos));
        }
    }

    @Override
    protected DefaultedList<ItemStack> createSlotsList() {
        return DefaultedList.of();
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        if (this.mainPos != null) {
            BlockEntity main = this.getWorld().getBlockEntity(this.mainPos);
            if (main instanceof MixingColumnBlockEntity) {
                return ((MixingColumnBlockEntity) main).createScreenHandler(syncId, playerInventory);
            }
        }
        return null;
    }

    @Override
    public Text getContainerName() {
        return Text.translatable("block.magifacture.mixing_column");
    }

    public void onStateChange(BlockState newState) {
        if (newState.get(Properties.DOWN)) {
            for (BlockPos pos = this.pos.mutableCopy(); pos.getY() > this.pos.getY() - 6; pos = pos.down()) {
                BlockState state = this.getWorld().getBlockState(pos);
                if (state.isOf(MixingColumnBlock.INSTANCE) //
                        && !state.get(Properties.DOWN)) {
                    this.mainPos = pos;
                    return;
                }
            }
        } else {
            this.world.removeBlockEntity(this.pos);
            MixingColumnBlockEntity newBe = new MixingColumnBlockEntity(this.pos, this.world.getBlockState(this.pos));
            this.world.addBlockEntity(newBe);
            newBe.onStateChange(newState);
        }
    }
}
