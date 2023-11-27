package magifacture.screen;

import magifacture.block.entity.InfuserBlockEntity;
import magifacture.screen.slot.SimpleOutputSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class InfuserScreenHandler extends MagifactureScreenHandler {
    public static final ScreenHandlerType<InfuserScreenHandler> TYPE = new ScreenHandlerType<>(InfuserScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
    public final PropertyDelegate properties;
    public final SingleFluidStorage tank;

    private InfuserScreenHandler(int syncId, Inventory inventory, SingleFluidStorage tank, PlayerInventory playerInv, PropertyDelegate properties) {
        super(TYPE, syncId, inventory);
        this.tank = tank;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlot(new Slot(inventory, x + y * 3, 62 + x * 18, 17 + y * 18));
            }
        }

        this.addSlot(new SimpleOutputSlot(inventory, InfuserBlockEntity.SLOT_OUTPUT, 148, 35));

        this.addSlot(new Slot(this.inventory, InfuserBlockEntity.SLOT_FULL, 8, 17));
        this.addSlot(new SimpleOutputSlot(this.inventory, InfuserBlockEntity.SLOT_DRAINED, 8, 53));

        this.addPlayerSlots(playerInv);
        this.addNbtSlot(this.tank::readNbt, this.tank::writeNbt);
        this.addProperties(this.properties = properties);
    }

    public InfuserScreenHandler(int syncId, InfuserBlockEntity be, PlayerInventory playerInv) {
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

    public InfuserScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, new SimpleInventory(InfuserBlockEntity.INVENTORY_SIZE), SingleFluidStorage.withFixedCapacity(InfuserBlockEntity.FLUID_CAPACITY, () -> {
        }), playerInv, new ArrayPropertyDelegate(2));
    }
}
