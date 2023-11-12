package reoseah.magifacture.screen;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import reoseah.magifacture.block.entity.CrematoriumBlockEntity;
import reoseah.magifacture.screen.slot.MagifactureFuelSlot;
import reoseah.magifacture.screen.slot.MagifactureOutputSlot;
import reoseah.magifacture.util.FluidUtils;

public class CrematoriumScreenHandler extends MagifactureScreenHandler {
    public static final ScreenHandlerType<CrematoriumScreenHandler> TYPE = new ScreenHandlerType<>(CrematoriumScreenHandler::new);
    public final PropertyDelegate properties;
    public final SingleFluidStorage tank;

    private CrematoriumScreenHandler(int syncId, Inventory inventory, SingleFluidStorage tank, PlayerInventory playerInv, PropertyDelegate properties) {
        super(TYPE, syncId, inventory);

        this.addSlot(new Slot(this.inventory, 0, 18, 17));
        this.addSlot(new MagifactureFuelSlot(this.inventory, 1, 18, 53));
        this.addSlot(new MagifactureOutputSlot(this.inventory, 2, 78, 35));
        this.addSlot(new Slot(this.inventory, 3, 143, 17));
        this.addSlot(new MagifactureOutputSlot(this.inventory, 4, 143, 53));

        this.addPlayerSlots(playerInv);

        this.addTank(this.tank = tank);
        this.addProperties(this.properties = properties);
    }

    public CrematoriumScreenHandler(int syncId, CrematoriumBlockEntity be, PlayerInventory playerInv) {
        this(syncId, be, be.getTank(), playerInv, new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getFuelLeft();
                    case 1 -> be.getFuelDuration();
                    case 2 -> be.getRecipeProgress();
                    case 3 -> be.getRecipeDuration();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // unused on server
            }

            @Override
            public int size() {
                return 4;
            }
        });
    }

    public CrematoriumScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, new SimpleInventory(5), SingleFluidStorage.withFixedCapacity(4000 * 81, () -> {
        }), playerInv, new ArrayPropertyDelegate(4));
    }

    @Override
    protected int getPreferredQuickMoveSlot(ItemStack stack, World world, int slot) {
        if (slot >= this.inventory.size()) {
            if (FluidUtils.canFillItem(stack, this.tank.variant.getFluid())) {
                return CrematoriumBlockEntity.EMPTY_SLOT;
            }
        }
        return super.getPreferredQuickMoveSlot(stack, world, slot);
    }
}
