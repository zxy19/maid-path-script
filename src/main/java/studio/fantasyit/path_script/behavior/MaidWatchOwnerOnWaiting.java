package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Map;

public class MaidWatchOwnerOnWaiting extends Behavior<EntityMaid> {

    public MaidWatchOwnerOnWaiting() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleRegistry.NEXT_NODE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long timestamp) {
        LivingEntity owner = maid.getOwner();
        if (owner == null) return;
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(owner, true));
    }
}