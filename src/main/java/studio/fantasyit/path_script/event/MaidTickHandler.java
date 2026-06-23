package studio.fantasyit.path_script.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidTickEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.path_script.MaidPathScriptTask;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

@EventBusSubscriber
public class MaidTickHandler {
    @SubscribeEvent
    public static void onMaidTick(MaidTickEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) return;
        maid.getData(AttachmentRegistry.PATH_GUIDE_MAID_FOR.get()).ifPresent(ownerUuid -> {
            if (maid.level() instanceof ServerLevel serverLevel) {
                Entity owner = serverLevel.getEntity(ownerUuid);
                if (owner == null || !owner.isAlive() || owner.level() != maid.level()) {
                    maid.discard();
                }
            }
        });
        if (maid.getTask().getUid().equals(MaidPathScriptTask.ID) && !maid.hasHome()) {
            maid.setHomeModeEnable(true);
        }
    }
}
