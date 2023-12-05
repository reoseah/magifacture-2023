package magifacture.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import magifacture.screen.widget.MultipleFluidDrawable;
import magifacture.screen.widget.SimpleFluidDrawable;
import magifacture.util.MultipleFluidStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.ObjIntConsumer;

@Environment(EnvType.CLIENT)
public abstract class MagifactureScreen<T extends MagifactureScreenHandler> extends HandledScreen<T> {
    public MagifactureScreen(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    public abstract Identifier getTextureId();

    protected SimpleFluidDrawable addTankRender(SingleFluidStorage tank, int x, int y, int width, int height) {
        SimpleFluidDrawable widget = new SimpleFluidDrawable(this, tank, x, y, width, height);
        this.addDrawable(widget);
        return widget;
    }

    protected MultipleFluidDrawable addTankRender(MultipleFluidStorage tank, int x, int y, int width, int height, ObjIntConsumer<FluidVariant> onClick) {
        MultipleFluidDrawable widget = new MultipleFluidDrawable(this, tank, x, y, width, height, onClick);
        this.addDrawable(widget);
        ((List<Element>) this.children()).add(widget);
        return widget;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, this.getTextureId());

        context.drawTexture(this.getTextureId(), this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    public int getTankOverlayU() {
        return this.backgroundWidth;
    }

    public int getTankOverlayV() {
        return 0;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
