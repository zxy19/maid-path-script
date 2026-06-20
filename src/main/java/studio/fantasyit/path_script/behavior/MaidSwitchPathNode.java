package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.util.MemoryUtil;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Map;
import java.util.Optional;

public class MaidSwitchPathNode extends Behavior<EntityMaid> {

    public MaidSwitchPathNode() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleRegistry.NEXT_NODE.get(), MemoryStatus.VALUE_PRESENT
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
        if (!BehaviorAndConditions.shouldSelectNextForMaid(maid, owner, cur.get(), path.get()))
            return false;
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long timestamp) {
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;
        Optional<PathSet> path = MemoryUtil.getPathSet(maid);
        if (path.isEmpty()) return;
        Optional<BlockPos> cur = MemoryUtil.getCurrentNode(maid);
        if (cur.isEmpty()) return;

        BlockPos pos = BehaviorAndConditions.getSelectedNextForMaid(maid, owner, cur.get(), path.get());
        if (pos == null) return;
        PathNode node = path.get().getNode(pos);
        if (node == null) return;

        BehaviorAndConditions.switchNodeTo(maid, node, path.get());
    }
}
