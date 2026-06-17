package studio.fantasyit.path_script;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

public class ServerTickHandler {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        for (ServerPlayer player : level.players()) {
            PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
            if (marker.pathingMaidEntity != null) {
                Entity entity = level.getEntity(marker.pathingMaidEntity);
                if (entity == null || !entity.isAlive()) {
                    player.setData(AttachmentRegistry.CLI_MARKER.get(), new PathMarker());
                }
            }
        }
    }
}
