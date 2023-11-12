package reoseah.magifacture.screen;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import reoseah.magifacture.block.entity.AlembicBlockEntity;
import reoseah.magifacture.screen.slot.MagifactureOutputSlot;
import reoseah.magifacture.util.FluidUtils;

public class AlembicScreenHandler extends MagifactureScreenHandler {
    public static final ScreenHandlerType<AlembicScreenHandler> TYPE = new ScreenHandlerType<>(AlembicScreenHandler::new);
    protected final SingleFluidStorage tank;

    private AlembicScreenHandler(int syncId, Inventory inventory, SingleFluidStorage tank, PlayerInventory playerInv) {
        super(TYPE, syncId, inventory);
        this.addSlot(new Slot(this.inventory, 0, 98, 18));
        this.addSlot(new MagifactureOutputSlot(this.inventory, 1, 98, 54));
        this.addPlayerSlots(playerInv);

        this.addTank(this.tank = tank);
    }

    public AlembicScreenHandler(int syncId, AlembicBlockEntity be, PlayerInventory playerInv) {
        this(syncId, be, be.getTank(), playerInv);
    }

    public AlembicScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, new SimpleInventory(2), SingleFluidStorage.withFixedCapacity(4000 * 81, () -> {
        }), playerInv);
    }

    public SingleFluidStorage getTank() {
        return this.tank;
    }

    @Override
    protected int getPreferredQuickMoveSlot(ItemStack stack, World world, int slot) {
        if (slot >= this.inventory.size() && FluidUtils.canFillItem(stack, this.tank.variant.getFluid())) {
            return 0;
        }
        return super.getPreferredQuickMoveSlot(stack, world, slot);
    }
}
