package magifacture.screen.widget;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import magifacture.screen.MagifactureScreen;
import magifacture.util.FluidRendering;
import magifacture.util.FluidTexts;
import magifacture.util.MultipleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.fluid.Fluids;
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
        int total = 0;
        for (Object2LongMap.Entry<FluidVariant> entry : this.storage.getFluids().object2LongEntrySet()) {
            FluidVariant variant = entry.getKey();
            long amount = entry.getLongValue();

            if (!variant.isBlank()) {
                int height = MathHelper.clamp(Math.round(amount * (float) this.height / this.storage.getCapacity()), 1, this.height - total);
                int y = this.screen.getY() + this.y + MathHelper.clamp(this.height - height - total, 0, this.height - total);

                FluidRendering.drawFluidColumn(context, variant, //
                        this.screen.getX() + this.x, y, height, 1);
                if (this.width > 16) {
                    FluidRendering.drawFluidColumn(context, variant, //
                            this.screen.getX() + this.x + 16, y, height, 1);
                }
                total += height;
            }
        }


        context.drawTexture(this.screen.getTextureId(), //
                this.screen.getX() + this.x, //
                this.screen.getY() + this.y, //
                this.screen.getTankOverlayU(), //
                this.screen.getTankOverlayV(), //
                this.width, this.height);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (this.isMouseOver(mouseX, mouseY)) {
            int totalHeight = 0;
            long totalAmount = 0;
            for (Object2LongMap.Entry<FluidVariant> entry : this.storage.getFluids().object2LongEntrySet()) {
                FluidVariant variant = entry.getKey();
                long amount = entry.getLongValue();

                if (!variant.isBlank()) {
                    int fluidHeight = MathHelper.clamp(Math.round(amount * (float) this.height / this.storage.getCapacity()), 1, this.height);

                    if (mouseX >= screen.getX() + this.x && mouseX < screen.getX() + this.x + this.width //
                            && mouseY >= screen.getY() + this.y + this.height - fluidHeight - totalHeight //
                            && mouseY < screen.getY() + this.y + this.height - totalHeight) {
                        List<Text> tooltip = FluidTexts.getTooltip(variant.getFluid(), amount, (int) storage.getCapacity());
                        context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                    }

                    totalHeight += fluidHeight;
                    totalAmount += amount;
                }
            }
            if (mouseY < screen.getY() + this.y + this.height - totalHeight) {
                long capacity = storage.getCapacity();
                List<Text> tooltip = FluidTexts.getTooltip(Fluids.EMPTY, capacity - totalAmount, capacity);
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= screen.getX() + this.x && mouseX < screen.getX() + this.x + this.width //
                && mouseY >= screen.getY() + this.y && mouseY < screen.getY() + this.y + this.height;
    }
}
