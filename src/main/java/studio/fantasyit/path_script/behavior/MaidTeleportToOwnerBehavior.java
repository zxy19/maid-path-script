package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.Config;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;
import studio.fantasyit.path_script.util.MarkUtil;
import studio.fantasyit.path_script.util.MemoryUtil;

import java.util.Map;
import java.util.Optional;

public class MaidTeleportToOwnerBehavior extends Behavior<EntityMaid> {

    public MaidTeleportToOwnerBehavior() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleRegistry.NEXT_NODE.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (owner == null || owner.level() != maid.level()) return true;
        Optional<PathSet> path = MemoryUtil.getPathSet(maid);
        if (path.isEmpty()) return false;
        Optional<BlockPos> cur = MemoryUtil.getCurrentNode(maid);
        if (cur.isEmpty()) return false;
        double maxDist = Config.teleportMaxDistance;
        return maid.distanceToSqr(owner) > maxDist * maxDist;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long timestamp) {
        LivingEntity owner = maid.getOwner();
        if (owner == null || owner.level() != maid.level()) {
            maid.getBrain().eraseMemory(MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get());
            maid.getBrain().eraseMemory(MemoryModuleRegistry.NEXT_NODE.get());
            return;
        }
        Optional<PathSet> path = MemoryUtil.getPathSet(maid);
        if (path.isEmpty()) return;

        PathNode nearest = path.get().getNearest(owner.blockPosition());
        if (nearest == null) return;
        double clearDist = Config.clearDistance;
        if (nearest.pos().distSqr(owner.blockPosition()) > clearDist * clearDist) {
            MarkUtil.clearMarker(owner);
            if (owner.hasData(AttachmentRegistry.GUIDE_MAID) && owner.getData(AttachmentRegistry.GUIDE_MAID).map(t -> t.equals(maid.getUUID())).orElse(false)) {
                maid.discard();
            } else {
                maid.getBrain().eraseMemory(MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get());
                maid.getBrain().eraseMemory(MemoryModuleRegistry.NEXT_NODE.get());
            }
            return;
        }

        BlockPos target = nearest.pos();
        maid.teleportTo(target.getCenter().x(), target.getCenter().y(), target.getCenter().z());
        MarkUtil.resetMarker(owner);
        BehaviorAndConditions.switchNodeTo(maid, nearest, path.get());
    }
}
