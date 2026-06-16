package studio.fantasyit.path_script.reg;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.data.PathSet;

import java.util.Optional;

public class MemoryModuleRegistry {
    public static final DeferredRegister<MemoryModuleType<?>> REGISTER
            = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, PathScript.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<PathSet>> CURRENT_PATH_SCRIPT
            = REGISTER.register("current_path_script", () -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> NEXT_NODE
            = REGISTER.register("next_node", () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
