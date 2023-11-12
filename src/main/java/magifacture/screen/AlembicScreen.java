package magifacture.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AlembicScreen extends MagifactureScreen<AlembicScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("magifacture", "textures/gui/alembic.png");

    public AlembicScreen(AlembicScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.addTankRender(handler.getTank(), 71, 19, 16, 50);
    }

    @Override
    protected Identifier getTextureId() {
        return TEXTURE;
    }
}
