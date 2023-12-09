package magifacture.item;

import magifacture.fluid.MoltenMagicCrystalFluid;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoltenMagicCrystalBucket extends Item {
    public static final Item INSTANCE = new MoltenMagicCrystalBucket(new Item.Settings().recipeRemainder(Items.BUCKET).rarity(Rarity.UNCOMMON).maxCount(1));

    public MoltenMagicCrystalBucket(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        MoltenMagicCrystalFluid.appendTooltip(nbt, tooltip);
    }
}
