package reoseah.magifacture.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import reoseah.magifacture.client.FluidClientUtils;
import reoseah.magifacture.screen.MagifactureScreenHandler;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class MagifactureScreen<T extends MagifactureScreenHandler> extends HandledScreen<T> {
    private final List<TankArea> tanks = new ArrayList<>();

    public MagifactureScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    protected abstract Identifier getTextureId();

    protected TankArea addTankRender(SingleFluidStorage tank, int x, int y, int width, int height) {
        TankArea display = new TankArea(tank, x, y, width, height);
        this.tanks.add(display);
        return display;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, this.getTextureId());

        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        for (TankArea display : this.tanks) {
            FluidVariant volume = display.tank.variant;
            if (!volume.isBlank()) {
                int fluidHeight = MathHelper.clamp(Math.round(display.tank.amount * (float) display.height / display.tank.getCapacity()), 1, display.height);

                FluidClientUtils.drawFluidColumn(matrices, volume, this.x + display.x, this.y + display.y + display.height - fluidHeight, fluidHeight, this.getZOffset());
                if (display.width > 16) {
                    FluidClientUtils.drawFluidColumn(matrices, volume, this.x + display.x + 16, this.y + display.y + display.height - fluidHeight, fluidHeight, this.getZOffset());
                }
            }
        }
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, this.getTextureId());

        for (TankArea tank : this.tanks) {
            this.drawTexture(matrices, this.x + tank.x, this.y + tank.y, this.getTankOverlayU(), this.getTankOverlayV(), tank.width, tank.height);
        }
    }

    protected int getTankOverlayU() {
        return this.backgroundWidth;
    }

    protected int getTankOverlayV() {
        return 0;
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);

        for (TankArea tank : this.tanks) {
            if (this.isPointWithinBounds(tank.x, tank.y, tank.width, tank.height, mouseX, mouseY)) {
                List<Text> tooltip = tank.getTooltip();
                if (!tooltip.isEmpty()) {
                    this.renderTooltip(matrices, tooltip, mouseX, mouseY);
                }
            }
        }
    }

    // note: only width=16 and 32 are supported
    public static final class TankArea {
        private final SingleFluidStorage tank;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public TankArea(SingleFluidStorage tank, int x, int y, int width, int height) {
            this.tank = tank;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public List<Text> getTooltip() {
            return FluidClientUtils.getTooltipWithCapacity(this.tank.variant.getFluid(), (int) tank.amount, (int) tank.getCapacity());
        }
    }
}
