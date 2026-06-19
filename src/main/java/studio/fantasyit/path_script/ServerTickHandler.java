package studio.fantasyit.path_script;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import studio.fantasyit.path_script.behavior.BehaviorAndConditions;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.memory.MemoryUtil;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.Optional;
import java.util.UUID;

public class ServerTickHandler {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        for (ServerPlayer player : level.players()) {
            BehaviorAndConditions.clearClientMarkerIfInvalid(player, level);

            Optional<UUID> guideUuid = player.getData(AttachmentRegistry.GUIDE_MAID.get());
            guideUuid.ifPresent(uuid -> {
                Entity entity = level.getEntity(uuid);
                if (!(entity instanceof EntityMaid maid) || !maid.isAlive()) {
                    player.setData(AttachmentRegistry.GUIDE_MAID.get(), Optional.empty());
                    return;
                }
                if (!maid.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
                    maid.discard();
                    player.setData(AttachmentRegistry.GUIDE_MAID.get(), Optional.empty());
                    return;
                }
                Optional<PathSet> pathSet = MemoryUtil.getPathSet(maid);
                Optional<BlockPos> currentNode = MemoryUtil.getCurrentNode(maid);
                if (pathSet.isPresent() && currentNode.isPresent()) {
                    var node = pathSet.get().getNode(currentNode.get());
                    if (node != null && node.next().isEmpty() && maid.distanceToSqr(currentNode.get().getCenter()) < 4) {
                        maid.discard();
                        player.setData(AttachmentRegistry.GUIDE_MAID.get(), Optional.empty());
                    }
                }
            });
        }
    }
}
