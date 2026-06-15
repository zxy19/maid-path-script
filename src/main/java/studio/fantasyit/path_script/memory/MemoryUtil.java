package studio.fantasyit.path_script.memory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.Optional;

public class MemoryUtil {
    public static Optional<PathSet> getPathSet(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get());
    }
    public static Optional<BlockPos> getCurrentNode(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleRegistry.CURRENT_NODE.get());
    }
    public static void setPathSet(EntityMaid maid, PathSet pathSet) {
        maid.getBrain().setMemory(MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), pathSet);
    }
    public static void setCurrentNode(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleRegistry.CURRENT_NODE.get(), pos);
    }
}
