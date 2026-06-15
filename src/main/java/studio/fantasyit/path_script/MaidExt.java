package studio.fantasyit.path_script;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.List;

@LittleMaidExtension
public class MaidExt implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        ILittleMaid.super.addMaidTask(manager);
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        ILittleMaid.super.addExtraMaidBrain(manager);
        manager.addExtraMaidBrain(new IExtraMaidBrain() {
            @Override
            public List<MemoryModuleType<?>> getExtraMemoryTypes() {
                return List.of(
                        MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(),
                        MemoryModuleRegistry.CURRENT_NODE.get()
                );
            }

            @Override
            public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getCoreBehaviors() {
                return IExtraMaidBrain.super.getCoreBehaviors();
            }
        });
    }
}
