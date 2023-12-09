package magifacture.screen.client;

import magifacture.screen.CrematoriumScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CrematoriumScreen extends MagifactureScreen<CrematoriumScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("magifacture", "textures/gui/crematorium.png");

    public CrematoriumScreen(CrematoriumScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.addTankRender(handler.tank, 116, 18, 16, 50);
    }

    @Override
    public Identifier getTextureId() {
        return TEXTURE;
    }

    @Override
    public int getTankOverlayU() {
        return 208;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        super.drawBackground(context, delta, mouseX, mouseY);

        int fuelLeft = this.handler.properties.get(0);
        if (fuelLeft > 0) {
            int fuelDuration = this.handler.properties.get(1);
            int fuelGauge = fuelLeft * 13 / (fuelDuration == 0 ? 200 : fuelDuration);
            context.drawTexture(TEXTURE, this.x + 18, this.y + 36 + 12 - fuelGauge, 176, 12 - fuelGauge, 14, fuelGauge + 1);
        }

        int recipeProgress = this.handler.properties.get(2);
        if (recipeProgress > 0) {
            int recipeDuration = this.handler.properties.get(3);
            int gauge = recipeProgress * 24 / (recipeDuration == 0 ? 400 : recipeDuration);
            context.drawTexture(TEXTURE, x + 41, y + 34, 176, 14, gauge + 1, 16);
        }
    }
}
