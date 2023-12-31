package magifacture.screen;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import magifacture.block.entity.MixingColumnBlockEntity;
import magifacture.fluid.transfer.FluidTransferUtils;
import magifacture.fluid.transfer.MultipleFluidStorage;
import magifacture.screen.slot.SimpleOutputSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class MixingColumnScreenHandler extends MagifactureScreenHandler {
    public static final ScreenHandlerType<MixingColumnScreenHandler> TYPE = new ScreenHandlerType<>(MixingColumnScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public final MultipleFluidStorage storage;

    protected MixingColumnScreenHandler(int syncId, Inventory inventory, MultipleFluidStorage storage, PlayerInventory playerInv) {
        super(TYPE, syncId, inventory);
        this.storage = storage;

        this.addSlot(new Slot(this.inventory, MixingColumnBlockEntity.SLOT_INPUT, 36, 35));
        this.addSlot(new SimpleOutputSlot(this.inventory, MixingColumnBlockEntity.SLOT_OUTPUT, 124, 35));

        this.addSlot(new Slot(this.inventory, MixingColumnBlockEntity.SLOT_TO_DRAIN, 8, 17));
        this.addSlot(new SimpleOutputSlot(this.inventory, MixingColumnBlockEntity.SLOT_DRAINED, 8, 53));
        this.addSlot(new Slot(this.inventory, MixingColumnBlockEntity.SLOT_TO_FILL, 151, 17));
        this.addSlot(new SimpleOutputSlot(this.inventory, MixingColumnBlockEntity.SLOT_FILLED, 151, 53));

        this.addPlayerSlots(playerInv);
    }

    public MixingColumnScreenHandler(int syncId, MixingColumnBlockEntity be, PlayerInventory playerInv) {
        this(syncId, be, be.getFluidStorage(), playerInv);

        this.addNbtSlot(this.storage::readNbt, be.getFluidStorage()::writeNbtWithCapacity);
    }

    public MixingColumnScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, //
                new SimpleInventory(MixingColumnBlockEntity.INVENTORY_SIZE), //
                MultipleFluidStorage.withMutableCapacity(4000 * 81), //
                playerInv);

        this.addNbtSlot(this.storage::readNbt, this.storage::writeNbt);
    }

    @Override
    protected int getPreferredQuickMoveSlot(ItemStack stack, World world, int slot) {
        if (slot >= this.inventory.size()) {
            if (FluidTransferUtils.canFill(stack)) {
                return MixingColumnBlockEntity.SLOT_TO_FILL;
            } else if (FluidTransferUtils.canDrain(stack)) {
                return MixingColumnBlockEntity.SLOT_TO_DRAIN;
            }
        }
        return super.getPreferredQuickMoveSlot(stack, world, slot);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        Object2LongMap<FluidVariant> fluids = this.storage.getFluidMap();
        if (id != 0 && id < fluids.size()) {
            FluidVariant fluid = fluids.keySet().stream().skip(id).findFirst().orElse(FluidVariant.blank());
            long amount = fluids.getLong(fluid);

            Object2LongMap<FluidVariant> copy = new Object2LongLinkedOpenHashMap<>(fluids);
            copy.remove(fluid, amount);

            fluids.clear();
            fluids.put(fluid, amount);
            fluids.putAll(copy);

            this.inventory.markDirty();

            return true;
        }
        return super.onButtonClick(player, id);
    }
}
