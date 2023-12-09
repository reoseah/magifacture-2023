package magifacture.screen.client;

import magifacture.screen.InfuserScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class InfuserScreen extends MagifactureScreen<InfuserScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("magifacture", "textures/gui/infuser.png");

    public InfuserScreen(InfuserScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.addTankRender(handler.tank, 35, 18, 16, 50);
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

        int recipeProgress = this.handler.properties.get(0);
        if (recipeProgress > 0) {
            int recipeDuration = this.handler.properties.get(1);
            int gauge = recipeProgress * 17 / (recipeDuration == 0 ? 100 : recipeDuration);
            context.drawTexture(TEXTURE, x + 120, y + 34, 176, 14, gauge + 1, 16);
        }
    }
}
