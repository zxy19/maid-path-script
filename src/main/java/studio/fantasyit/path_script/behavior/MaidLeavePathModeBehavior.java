package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Map;

public class MaidLeavePathModeBehavior extends Behavior<EntityMaid> {
    public MaidLeavePathModeBehavior() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected void start(ServerLevel level, EntityMaid body, long timestamp) {
        body.setTask(TaskManager.getIdleTask());
    }
}
