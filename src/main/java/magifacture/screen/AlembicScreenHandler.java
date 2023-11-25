package magifacture.screen;

import lombok.Getter;
import magifacture.block.entity.AlembicBlockEntity;
import magifacture.util.SerializableSingleFluidStorage;
import magifacture.util.FluidUtils;
import magifacture.screen.slot.SimpleOutputSlot;
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
    protected final SerializableSingleFluidStorage tank;

    private AlembicScreenHandler(int syncId, Inventory inventory, SerializableSingleFluidStorage tank, PlayerInventory playerInv) {
        super(TYPE, syncId, inventory);
        this.addSlot(new Slot(this.inventory, 0, 98, 18));
        this.addSlot(new SimpleOutputSlot(this.inventory, 1, 98, 54));
        this.addPlayerSlots(playerInv);

        this.addNbtSerializable(this.tank = tank);
    }

    public AlembicScreenHandler(int syncId, AlembicBlockEntity be, PlayerInventory playerInv) {
        this(syncId, be, be.getTank(), playerInv);
    }

    public AlembicScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, new SimpleInventory(2), SerializableSingleFluidStorage.withFixedCapacity(4000 * 81, () -> {
        }), playerInv);
    }

    @Override
    protected int getPreferredQuickMoveSlot(ItemStack stack, World world, int slot) {
        if (slot >= this.inventory.size() && FluidUtils.canFillItem(stack, this.tank.variant.getFluid())) {
            return 0;
        }
        return super.getPreferredQuickMoveSlot(stack, world, slot);
    }
}
