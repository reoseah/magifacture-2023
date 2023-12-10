package magifacture.screen;

import lombok.Getter;
import magifacture.block.entity.AlembicBlockEntity;
import magifacture.fluid.transfer.FluidTransferUtils;
import magifacture.screen.slot.SimpleOutputSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class AlembicScreenHandler extends MagifactureScreenHandler {
    public static final ScreenHandlerType<AlembicScreenHandler> TYPE = new ScreenHandlerType<>(AlembicScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
    @Getter
    protected final SingleFluidStorage tank;

    private AlembicScreenHandler(int syncId, Inventory inventory, SingleFluidStorage tank, PlayerInventory playerInv) {
        super(TYPE, syncId, inventory);
        this.addSlot(new Slot(this.inventory, 0, 98, 18));
        this.addSlot(new SimpleOutputSlot(this.inventory, 1, 98, 54));
        this.addPlayerSlots(playerInv);
        this.tank = tank;
        this.addNbtSlot(this.tank::readNbt, this.tank::writeNbt);
    }

    public AlembicScreenHandler(int syncId, AlembicBlockEntity be, PlayerInventory playerInv) {
        this(syncId, be, be.getTank(), playerInv);
    }

    public AlembicScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, new SimpleInventory(2), SingleFluidStorage.withFixedCapacity(4000 * 81, () -> {
        }), playerInv);
    }

    @Override
    protected int getPreferredQuickMoveSlot(ItemStack stack, World world, int slot) {
        if (slot >= this.inventory.size() && FluidTransferUtils.canFillWith(stack, this.tank.variant)) {
            return 0;
        }
        return super.getPreferredQuickMoveSlot(stack, world, slot);
    }
}
