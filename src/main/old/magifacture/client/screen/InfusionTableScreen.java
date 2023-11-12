package reoseah.magifacture.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import reoseah.magifacture.screen.InfusionTableScreenHandler;

@Environment(EnvType.CLIENT)
public class InfusionTableScreen extends MagifactureScreen<InfusionTableScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("magifacture", "textures/gui/infusion_table.png");

    public InfusionTableScreen(InfusionTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.addTankRender(handler.tank, 35, 18, 16, 50);
    }

    @Override
    protected Identifier getTextureId() {
        return TEXTURE;
    }

    @Override
    protected int getTankOverlayU() {
        return 208;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        super.drawBackground(matrices, delta, mouseX, mouseY);

        int recipeProgress = this.handler.properties.get(0);
        if (recipeProgress > 0) {
            int recipeDuration = this.handler.properties.get(1);
            int gauge = recipeProgress * 17 / (recipeDuration == 0 ? 100 : recipeDuration);
            this.drawTexture(matrices, x + 120, y + 34, 176, 14, gauge + 1, 16);
        }
    }
}
