package magifacture.item;

import magifacture.fluid.ExperienceFluid;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ExperienceBucketItem extends BucketItem {
    public static final Item INSTANCE = new ExperienceBucketItem(ExperienceFluid.INSTANCE, new Settings().rarity(Rarity.RARE).maxCount(1).recipeRemainder(Items.BUCKET));

    protected ExperienceBucketItem(Fluid fluid, Settings settings) {
        super(fluid, settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        user.addExperience(ExperienceFluid.XP_PER_BUCKET);

        ItemStack ret = ItemUsage.exchangeStack(stack, user, new ItemStack(Items.BUCKET));

        return TypedActionResult.success(ret, world.isClient());
    }
}
