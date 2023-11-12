package reoseah.magifacture.screen;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import reoseah.magifacture.block.entity.InfusionTableBlockEntity;
import reoseah.magifacture.screen.slot.MagifactureOutputSlot;

public class InfusionTableScreenHandler extends MagifactureScreenHandler {
    public static final ScreenHandlerType<InfusionTableScreenHandler> TYPE = new ScreenHandlerType<>(InfusionTableScreenHandler::new);
    public final PropertyDelegate properties;
    public final SingleFluidStorage tank;

    private InfusionTableScreenHandler(int syncId, Inventory inventory, SingleFluidStorage tank, PlayerInventory playerInv, PropertyDelegate properties) {
        super(TYPE, syncId, inventory);

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlot(new Slot(inventory, x + y * 3, 62 + x * 18, 17 + y * 18));
            }
        }

        this.addSlot(new MagifactureOutputSlot(inventory, InfusionTableBlockEntity.SLOT_OUTPUT, 148, 35));

        this.addSlot(new Slot(this.inventory, InfusionTableBlockEntity.SLOT_FULL, 8, 17));
        this.addSlot(new MagifactureOutputSlot(this.inventory, InfusionTableBlockEntity.SLOT_DRAINED, 8, 53));

        this.addPlayerSlots(playerInv);

        this.addTank(this.tank = tank);
        this.addProperties(this.properties = properties);
    }

    public InfusionTableScreenHandler(int syncId, InfusionTableBlockEntity be, PlayerInventory playerInv) {
        this(syncId, be, be.getTank(), playerInv, new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> be.getRecipeProgress();
                    case 1 -> be.getRecipeDuration();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // unused on server
            }

            @Override
            public int size() {
                return 2;
            }
        });
    }

    public InfusionTableScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, new SimpleInventory(InfusionTableBlockEntity.INVENTORY_SIZE), SingleFluidStorage.withFixedCapacity(InfusionTableBlockEntity.CAPACITY_MB * 81, () -> {
        }), playerInv, new ArrayPropertyDelegate(2));
    }
}
