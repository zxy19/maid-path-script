package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;

public class BehaviorConditions {

    public static boolean isOwnerAvailableForMove(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        if (pathSet.getNodes().isEmpty()) return false;
        PathNode playerNearest = pathSet.getNearest(owner.blockPosition());
        if (playerNearest.pos().equals(nextPos)) {
            return true;
        }
        BlockPos pref = pathSet.getParent(nextPos);
        if (pref != null && pref.equals(playerNearest.pos())) {
            return true;
        }
        PathNode maidNearest = pathSet.getNearest(maid.blockPosition());
        BlockPos maidPref = pathSet.getParent(maidNearest.pos());
        if (maidPref != null && maidPref.equals(playerNearest.pos())) {
            return true;
        }
        return isOwnerAheadOfMaid(maid, owner, nextPos, pathSet);
    }

    public static boolean isOwnerAheadOfMaid(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        PathNode playerNearest = pathSet.getNearest(owner.blockPosition());
        if (pathSet.isAncestor(nextPos, playerNearest.pos())) {
            return true;
        }
        return false;
    }

    public static boolean shouldSelectNextForMaid(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        PathNode node = pathSet.getNode(nextPos);
        if (node.next().isEmpty()) return false;
        return node.next().size() == 1 || isOwnerAheadOfMaid(maid, owner, nextPos, pathSet);
    }

    public static @Nullable BlockPos getSelectedNextForMaid(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        PathNode node = pathSet.getNode(nextPos);
        if (node.next().isEmpty()) return null;
        if (node.next().size() == 1) return node.next().getFirst();
        PathNode playerNearest = pathSet.getNearest(owner.blockPosition());
        for (BlockPos nxt : node.next()) {
            if (playerNearest.pos().equals(nxt) || pathSet.isAncestor(nxt, playerNearest.pos())) {
                return nxt;
            }
        }
        return null;
    }

    public static boolean canSwitchToNext(EntityMaid maid, BlockPos nextPos, PathSet pathSet) {
        PathNode node = pathSet.getNode(nextPos);
        return !node.next().isEmpty();
    }
}
