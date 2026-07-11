package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.path_script.Config;
import studio.fantasyit.path_script.MaidPathScriptTask;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.util.MarkUtil;
import studio.fantasyit.path_script.util.MemoryUtil;
import studio.fantasyit.path_script.util.MessageUtil;

import java.util.Set;

public class BehaviorAndConditions {

    private static final int MARKER_DEPTH = 3;

    public static boolean isOwnerAvailableForMove(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        if (pathSet.getNodes().isEmpty()) return false;
        PathNode maidNearest = pathSet.getNearest(maid.blockPosition(), pathSet.getNode(nextPos));
        PathNode playerNearest = pathSet.getNearest(owner.blockPosition(), pathSet.getNode(nextPos));
        if (playerNearest == null || maidNearest == null) return false;
        if (playerNearest.pos().equals(nextPos)) {
            return true;
        }
        Set<BlockPos> parents = pathSet.getParent(nextPos);
        if (parents.contains(playerNearest.pos())) {
            return true;
        }
        Set<BlockPos> maidParents = pathSet.getParent(maidNearest.pos());
        if (maidParents.contains(playerNearest.pos())) {
            return true;
        }
        return isOwnerAheadOfMaid(maid, owner, nextPos, pathSet);
    }

    public static boolean isOwnerAheadOfMaid(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        PathNode playerNearest = pathSet.getNearest(owner.blockPosition(), pathSet.getNode(nextPos));
        if (playerNearest == null) return false;
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
        if (playerNearest == null) return null;
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


    public static void setUpMaidForPath(EntityMaid maid, PathSet pathSet, Player player, boolean forceTp, @Nullable Component welcomeMessage) {
        PathNode node = pathSet.getNearest(player.blockPosition());
        if (node == null || node.pos().distToCenterSqr(player.position()) > Config.clearDistance * Config.clearDistance)
            return;
        if (maid.getOwnerReference() == null) {
            maid.setOwner(player);
        } else if (!maid.getOwnerReference().matches(player)) {
            return;
        }
        maid.setHomeModeEnable(true);
        maid.getTaskManager().setTask(new MaidPathScriptTask());
        MemoryUtil.setPathSet(maid, pathSet);
        MemoryUtil.setCurrentNode(maid, node.pos());
        Vec3 center = node.pos().getCenter();
        maid.invulnerableTime = 5;
        if (forceTp) {
            maid.setPos(center.x, center.y - 0.5, center.z);
            maid.setOldPosAndRot();
        } else {
            maid.teleportTo(center.x, center.y - 0.5, center.z);
        }
        maid.fallDistance = 0;
        maid.setOnGround(true);
        maid.getNavigationManager().resetNavigation();
        if (welcomeMessage != null) {
            player.sendSystemMessage(MessageUtil.getMaidSentChat(maid, welcomeMessage));
        }
        switchNodeTo(maid, node, pathSet);
    }

    public static void switchNodeTo(EntityMaid maid, PathNode node, PathSet path) {
        BlockPos pos = node.pos();
        LivingEntity owner = maid.getOwner();
        if (owner instanceof ServerPlayer sp)
            for (IAction n : node.actions())
                n.onSwitchTo(sp, maid, pos);

        MemoryUtil.setCurrentNode(maid, pos);
        maid.setHomeTo(pos, maid.getHomeRadius());
        maid.getSchedulePos().setWorkPos(pos);
        BehaviorUtils.setWalkAndLookTargetMemories(maid, pos, 0.5f, 0);
        MarkUtil.updatePathMarker(maid, owner, path, pos);
    }
}
