package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.util.MemoryUtil;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Map;
import java.util.Optional;

public class MaidWaitOwnerBehavior extends Behavior<EntityMaid> {

    public MaidWaitOwnerBehavior() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleRegistry.NEXT_NODE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT
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
        return !BehaviorAndConditions.isOwnerAvailableForMove(maid, owner, cur.get(), path.get());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long timestamp) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(owner, true));
    }
}
