package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.path_script.Config;
import studio.fantasyit.path_script.MaidPathScriptTask;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.memory.MemoryUtil;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BehaviorAndConditions {

    private static final int MARKER_DEPTH = 3;

    public static boolean isOwnerAvailableForMove(EntityMaid maid, LivingEntity owner, BlockPos nextPos, PathSet pathSet) {
        if (pathSet.getNodes().isEmpty()) return false;
        PathNode playerNearest = pathSet.getNearest(owner.blockPosition());
        if (playerNearest.pos().equals(nextPos)) {
            return true;
        }
        Set<BlockPos> parents = pathSet.getParent(nextPos);
        if (parents.contains(playerNearest.pos())) {
            return true;
        }
        PathNode maidNearest = pathSet.getNearest(maid.blockPosition());
        Set<BlockPos> maidParents = pathSet.getParent(maidNearest.pos());
        if (maidParents.contains(playerNearest.pos())) {
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

    public static void updatePathMarker(EntityMaid maid, LivingEntity owner, PathSet pathSet, BlockPos currentNode) {
        if (!(owner instanceof ServerPlayer player)) return;

        PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
        marker.pathingMaidEntity = maid.getUUID();
        marker.lastUpdatedNode = currentNode;
        marker.tip.clear();
        marker.selectionPos.clear();
        marker.currentShowingTip = Component.empty();

        Path path = maid.getNavigation().createPath(currentNode, 0);
        List<BlockPos> newIndicator = new ArrayList<>();
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                newIndicator.add(path.getNodePos(i));
            }
        }
        marker.pathIndicatorLast = marker.pathIndicator;
        marker.pathIndicator = newIndicator;
        player.setData(AttachmentRegistry.CLI_MARKER.get(), marker);
    }

    public static void clearClientMarkerIfInvalid(ServerPlayer player, ServerLevel level) {
        PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
        if (marker.pathingMaidEntity == null) return;

        Entity entity = level.getEntity(marker.pathingMaidEntity);
        if (!(entity instanceof EntityMaid maid) || !maid.isAlive()) {
            player.setData(AttachmentRegistry.CLI_MARKER.get(), new PathMarker());
            return;
        }
        if (!maid.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            player.setData(AttachmentRegistry.CLI_MARKER.get(), new PathMarker());
        }
    }

    public static void setUpMaidForPath(EntityMaid maid, PathSet pathSet, Player player) {
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
        maid.teleportTo(center.x, center.y, center.z);
        maid.setHomeTo(node.pos(), maid.getHomeRadius());
        maid.getSchedulePos().setWorkPos(node.pos());
        maid.getNavigationManager().resetNavigation();
    }
}
