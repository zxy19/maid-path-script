package studio.fantasyit.path_script.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber
public class PlayerLogoutEvent {
    @SubscribeEvent
    public static void onPlayerLogoutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        Player entity = event.getEntity();
        if (entity.hasData(AttachmentRegistry.GUIDE_MAID)) {
            Optional<UUID> uuid = entity.getData(AttachmentRegistry.GUIDE_MAID);
            if (uuid.isPresent() && entity.level() instanceof ServerLevel sl && sl.getEntity(uuid.get()) instanceof EntityMaid maid) {
                maid.discard();
            }
        }
    }
}
