package magifacture.item;

import magifacture.fluid.MagicCrystalFluid;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This intentionally doesn't extend {@link BucketItem}
 * because we want to avoid default bucket behaviors
 * in Transfer API that ignore NBT data.
 */
public class MagicCrystalBucketItem extends Item implements FluidModificationItem {
    public static final Item INSTANCE = new MagicCrystalBucketItem(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    private final Fluid fluid = MagicCrystalFluid.Still.INSTANCE;

    public MagicCrystalBucketItem(Settings settings) {
        super(settings);
    }

    public static ItemStack createStack(float power, float purity, float heat) {
        ItemStack stack = new ItemStack(INSTANCE);
        stack.getOrCreateNbt().putFloat(MagicCrystalFluid.POWER_KEY, power);
        stack.getOrCreateNbt().putFloat(MagicCrystalFluid.PURITY_KEY, purity);
        stack.getOrCreateNbt().putFloat(MagicCrystalFluid.HEAT_KEY, heat);
        return stack;
    }

    public String getTranslationKey(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();

        float power = nbt != null ? nbt.getFloat(MagicCrystalFluid.POWER_KEY) : 0;
        float purity = nbt != null ? nbt.getFloat(MagicCrystalFluid.PURITY_KEY) : 0;

        if (power < 45 && purity < 45) {
            return "item.magifacture.molten_magic_crystal_bucket.low_power_low_purity";
        } else if (power < 90 && purity < 45) {
            return "item.magifacture.molten_magic_crystal_bucket.high_power_low_purity";
        } else if (power < 45 && purity < 90) {
            return "item.magifacture.molten_magic_crystal_bucket.low_power_high_purity";
        } else if (power < 90 && purity < 90) {
            return "item.magifacture.molten_magic_crystal_bucket.high_power_high_purity";
        }

        return this.getTranslationKey();
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        float power = nbt != null ? nbt.getFloat(MagicCrystalFluid.POWER_KEY) : 0;

        if (power < 45) {
            return Rarity.COMMON;
        }
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        MagicCrystalFluid.appendTooltip(nbt, tooltip);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(
                world, user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE
        );
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        } else {
            BlockPos pos = hitResult.getBlockPos();
            Direction direction = hitResult.getSide();
            BlockPos pos2 = pos.offset(direction);
            if (!world.canPlayerModifyAt(user, pos) || !user.canPlaceOn(pos2, direction, stack)) {
                return TypedActionResult.fail(stack);
            }
            BlockState state = world.getBlockState(pos);
            BlockPos placementPos = state.getBlock() instanceof FluidFillable && this.fluid == Fluids.WATER ? pos : pos2;
            if (this.placeFluid(user, world, placementPos, hitResult)) {
                this.onEmptied(user, world, stack, placementPos);
                if (user instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) user, placementPos, stack);
                }

                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(getEmptiedStack(stack, user), world.isClient());
            } else {
                return TypedActionResult.fail(stack);
            }
        }
    }

    @Override
    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        // TODO: make block keep fluid NBT when placed

        if (this.fluid instanceof FlowableFluid) {
            BlockState state = world.getBlockState(pos);
            boolean canReplace = state.canBucketPlace(this.fluid);
            Block block = state.getBlock();

            if (!state.isAir() && !canReplace) {
                if (block instanceof FluidFillable fillable //
                        && fillable.canFillWithFluid(player, world, pos, state, this.fluid)) {
                    return true;
                }
                if (hitResult != null) {
                    BlockPos offsetPos = hitResult.getBlockPos().offset(hitResult.getSide());
                    return this.placeFluid(player, world, offsetPos, null);
                }
            } else {
                if (!world.isClient && canReplace && !state.isLiquid()) {
                    world.breakBlock(pos, true);
                }
                if (world.setBlockState(pos, this.fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD)
                        || state.getFluidState().isStill()) {
                    this.playEmptyingSound(player, world, pos);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        return !player.getAbilities().creativeMode ? new ItemStack(Items.BUCKET) : stack;
    }

    protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        SoundEvent soundEvent = SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
        world.playSound(player, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
    }

    public static class FluidStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {
        private final ContainerItemContext context;

        public FluidStorage(ContainerItemContext context) {
            this.context = context;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (!this.context.getItemVariant().isOf(MagicCrystalBucketItem.INSTANCE)) {
                return 0;
            }

            if (resource.isOf(MagicCrystalFluid.Still.INSTANCE) && maxAmount >= FluidConstants.BUCKET) {
                ItemVariant emptyItem = ItemVariant.of(Items.BUCKET);
                if (context.exchange(emptyItem, 1, transaction) == 1) {
                    return FluidConstants.BUCKET;
                }
            }

            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return false;
        }

        @Override
        public FluidVariant getResource() {
            ItemVariant itemVariant = context.getItemVariant();
            NbtCompound itemNbt = itemVariant.getNbt();

            return FluidVariant.of(MagicCrystalFluid.Still.INSTANCE, MagicCrystalFluid.copyNbt(itemNbt));
        }

        @Override
        public long getAmount() {
            return FluidConstants.BUCKET;
        }

        @Override
        public long getCapacity() {
            return FluidConstants.BUCKET;
        }
    }
}
