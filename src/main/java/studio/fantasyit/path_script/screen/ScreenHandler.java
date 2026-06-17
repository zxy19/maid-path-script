package studio.fantasyit.path_script.screen;

import net.minecraft.client.Minecraft;
import studio.fantasyit.path_script.network.OpenNodeEditPacket;

public class ScreenHandler {
    public static void openEditor(OpenNodeEditPacket payload){
        Minecraft.getInstance().setScreen(
                new PathNodeEditScreen(payload.pos(), payload.actions())
        );
    }
}
