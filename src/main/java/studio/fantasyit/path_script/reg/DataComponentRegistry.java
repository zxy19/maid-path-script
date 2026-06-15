package studio.fantasyit.path_script.reg;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.data.PathSet;

public class DataComponentRegistry {
    public static final DeferredRegister<DataComponentType<?>> REGISTER
            = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PathScript.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> CURRENT_POS
            = REGISTER.register("current_pos", () -> DataComponentType.<BlockPos>builder()
            .persistent(BlockPos.CODEC)
            .networkSynchronized(BlockPos.STREAM_CODEC)
            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PathSet>> PATH_SET
            = REGISTER.register("path_set", () -> DataComponentType.<PathSet>builder()
            .persistent(PathSet.CODEC)
            .networkSynchronized(PathSet.STREAM_CODEC)
            .build());

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
