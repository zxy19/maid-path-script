package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Map;

public class MaidFollowPathMoveBehavior extends Behavior<EntityMaid> {
    public MaidFollowPathMoveBehavior() {
        super(Map.of(MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT));
    }

}
