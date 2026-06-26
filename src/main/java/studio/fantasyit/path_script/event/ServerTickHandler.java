package studio.fantasyit.path_script.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.ItemRegistry;
import studio.fantasyit.path_script.util.MarkUtil;
import studio.fantasyit.path_script.util.MemoryUtil;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber
public class ServerTickHandler {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        for (ServerPlayer player : level.players()) {
            MarkUtil.clearClientMarkerIfInvalid(player, level);
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

            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.is(ItemRegistry.GUIDE_SIGN)) {
                boolean hasGeneratedStored = mainHandItem.has(DataComponentRegistry.HAS_GENERATED_MAID);
                if (hasGeneratedStored != guideUuid.isPresent()) {
                    if (guideUuid.isPresent()) {
                        mainHandItem.set(DataComponentRegistry.HAS_GENERATED_MAID, true);
                    } else {
                        mainHandItem.remove(DataComponentRegistry.HAS_GENERATED_MAID);
                    }
                }
            }
        }
    }
}
