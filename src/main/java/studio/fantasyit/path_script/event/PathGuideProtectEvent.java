package studio.fantasyit.path_script.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import studio.fantasyit.path_script.PathScript;

@EventBusSubscriber
public class PathGuideProtectEvent {
    private static boolean isProtectedMaid(EntityMaid maid) {
        return maid.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"));
    }

    private static boolean isNotOwner(EntityMaid maid, Player player) {
        return !maid.isOwnedBy(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof EntityMaid maid
                && isProtectedMaid(maid)
                && isNotOwner(maid, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityMaid maid
                && isProtectedMaid(maid)
                && isNotOwner(maid, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof EntityMaid maid
                && isProtectedMaid(maid)
                && event.getSource().getEntity() instanceof Player player
                && isNotOwner(maid, player)) {
            event.setCanceled(true);
        }
    }
}
