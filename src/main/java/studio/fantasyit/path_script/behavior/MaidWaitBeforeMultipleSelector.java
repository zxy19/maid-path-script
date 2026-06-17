package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.memory.MemoryUtil;
import studio.fantasyit.path_script.reg.AttachmentRegistry;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Map;
import java.util.Optional;

public class MaidWaitBeforeMultipleSelector extends Behavior<EntityMaid> {
    public MaidWaitBeforeMultipleSelector() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleRegistry.NEXT_NODE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (owner == null || owner.level() != maid.level()) return false;
        Optional<PathSet> path = MemoryUtil.getPathSet(maid);
        if (path.isEmpty()) return false;
        Optional<BlockPos> cur = MemoryUtil.getCurrentNode(maid);
        if (cur.isEmpty()) return false;
        if (maid.distanceToSqr(cur.get().getCenter()) > 4) return false;
        if (!BehaviorConditions.shouldSelectNextForMaid(maid, owner, cur.get(), path.get()))
            return path.get().getNode(cur.get()).next().size() > 1;
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long timestamp) {
        if (!(maid.getOwner() instanceof ServerPlayer player)) return;
        PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
        marker.pathingMaidEntity = maid.getUUID();
        Optional<PathSet> path = MemoryUtil.getPathSet(maid);
        if (path.isEmpty()) return;
        Optional<BlockPos> cur = MemoryUtil.getCurrentNode(maid);
        if (cur.isEmpty()) return;
        marker.tip.clear();
        marker.currentShowingTip = Component.translatable("path.wait_before");
        for (BlockPos next : path.get().getNode(cur.get()).next()) {
            PathNode n = path.get().getNode(next);
            for(IAction a :n.actions()){
                if(a)
            }
        }
    }
}
