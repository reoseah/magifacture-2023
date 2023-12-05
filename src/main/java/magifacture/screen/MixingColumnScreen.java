package magifacture.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MixingColumnScreen extends MagifactureScreen<MixingColumnScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("magifacture", "textures/gui/mixing_column.png");

    public MixingColumnScreen(MixingColumnScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.addTankRender(handler.storage, 72, 18, 32, 50, (fluid, idx) -> {
            this.client.interactionManager.clickButton(this.getScreenHandler().syncId, idx);
        });
    }

    @Override
    public Identifier getTextureId() {
        return TEXTURE;
    }
}
