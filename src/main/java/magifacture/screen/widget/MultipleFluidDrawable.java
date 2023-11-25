package magifacture.screen.widget;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import magifacture.screen.MagifactureScreen;
import magifacture.util.FluidClientUtils;
import magifacture.util.MultipleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class MultipleFluidDrawable implements Drawable {
    protected final MagifactureScreen<?> screen;
    protected final MultipleFluidStorage storage;
    private final int x;
    private final int y;
    // only 16 and 32 supported
    private final int width;
    private final int height;

    public MultipleFluidDrawable(MagifactureScreen<?> screen, MultipleFluidStorage storage, int x, int y, int width, int height) {
        this.screen = screen;
        this.storage = storage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long totalAmount = 0;
        for (Object2LongMap.Entry<FluidVariant> entry : this.storage.getFluids().object2LongEntrySet()) {
            FluidVariant variant = entry.getKey();
            long amount = entry.getLongValue();

            if (!variant.isBlank()) {
                int fluidHeight = MathHelper.clamp(Math.round(amount * (float) this.height / this.storage.getCapacity()), 1, this.height);
                int offset = MathHelper.clamp(Math.round(totalAmount * (float) this.height / this.storage.getCapacity()), 1, this.height);

                FluidClientUtils.drawFluidColumn(context, variant, //
                        this.screen.getX() + this.x, //
                        this.screen.getY() + this.y + this.height - fluidHeight - offset, //
                        fluidHeight, 1);
                if (this.width > 16) {
                    FluidClientUtils.drawFluidColumn(context, variant, //
                            this.screen.getX() + this.x + 16, //
                            this.screen.getY() + this.y + this.height - fluidHeight, //
                            fluidHeight, 1);
                }
            }

            totalAmount += amount;
        }


        context.drawTexture(this.screen.getTextureId(), //
                this.screen.getX() + this.x, //
                this.screen.getY() + this.y, //
                this.screen.getTankOverlayU(), //
                this.screen.getTankOverlayV(), //
                this.width, this.height);

//        if (this.isMouseOver(mouseX, mouseY)) {
//            List<Text> tooltip = FluidClientUtils.getTooltipWithCapacity(variant.getFluid(), (int) storage.amount, (int) storage.getCapacity());
//            if (!tooltip.isEmpty()) {
//                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
//                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
//            }
//        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= screen.getX() + this.x && mouseX < screen.getX() + this.x + this.width //
                && mouseY >= screen.getY() + this.y && mouseY < screen.getY() + this.y + this.height;
    }
}
