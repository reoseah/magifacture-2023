package magifacture.screen.widget;

import magifacture.screen.client.MagifactureScreen;
import magifacture.screen.util.FluidRendering;
import magifacture.screen.util.FluidTexts;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class SimpleFluidWidget implements Drawable {
    protected final MagifactureScreen<?> screen;
    protected final SingleFluidStorage storage;
    private final int x;
    private final int y;
    // only 16 and 32 supported
    private final int width;
    private final int height;

    public SimpleFluidWidget(MagifactureScreen<?> screen, SingleFluidStorage storage, int x, int y, int width, int height) {
        this.screen = screen;
        this.storage = storage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        FluidVariant variant = this.storage.variant;

        if (!variant.isBlank()) {
            int fluidHeight = MathHelper.clamp(Math.round(this.storage.amount * (float) this.height / this.storage.getCapacity()), 1, this.height);

            FluidRendering.drawFluidColumn(context, variant, //
                    this.screen.getX() + this.x, //
                    this.screen.getY() + this.y + this.height - fluidHeight, //
                    fluidHeight, 1);
            if (this.width > 16) {
                FluidRendering.drawFluidColumn(context, variant, //
                        this.screen.getX() + this.x + 16, //
                        this.screen.getY() + this.y + this.height - fluidHeight, //
                        fluidHeight, 1);
            }
        }

        context.drawTexture(this.screen.getTextureId(), //
                this.screen.getX() + this.x, //
                this.screen.getY() + this.y, //
                this.screen.getTankOverlayU(), //
                this.screen.getTankOverlayV(), //
                this.width, this.height);

        if (this.isMouseOver(mouseX, mouseY)) {
            List<Text> tooltip = FluidTexts.getTooltip(variant, (int) storage.amount, (int) storage.getCapacity(), MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC);
            if (!tooltip.isEmpty()) {
                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= screen.getX() + this.x && mouseX < screen.getX() + this.x + this.width //
                && mouseY >= screen.getY() + this.y && mouseY < screen.getY() + this.y + this.height;
    }
}
