package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.IconAction;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.*;

public class BehaviorConditions {

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

        Set<BlockPos> nearbyNodes = collectNearbyNodes(pathSet, currentNode, MARKER_DEPTH);
        List<Pair<ItemStack, BlockPos>> allMarkers = new ArrayList<>();
        List<Pair<Component, BlockPos>> allTips = new ArrayList<>();
        for (BlockPos nodePos : nearbyNodes) {
            for (IAction action : pathSet.getAction(nodePos)) {
                if (action instanceof IconAction markerAction) {
                    for (Pair<ItemStack, BlockPos> entry : markerAction.markers()) {
                        allMarkers.add(Pair.of(entry.getFirst(), nodePos.offset(entry.getSecond())));
                    }
                    for (Pair<Component, BlockPos> entry : markerAction.tips()) {
                        allTips.add(Pair.of(entry.getFirst(), nodePos.offset(entry.getSecond())));
                    }
                }
            }
        }
        marker.markers = allMarkers;
        marker.tip = allTips;

        player.setData(AttachmentRegistry.CLI_MARKER.get(), marker);
    }

    private static Set<BlockPos> collectNearbyNodes(PathSet path, BlockPos center, int depth) {
        Set<BlockPos> result = new LinkedHashSet<>();
        result.add(center);
        Set<BlockPos> frontier = new HashSet<>();
        frontier.add(center);
        for (int d = 0; d < depth; d++) {
            Set<BlockPos> nextFrontier = new HashSet<>();
            for (BlockPos pos : frontier) {
                for (BlockPos next : path.getNext(pos)) {
                    if (result.add(next)) {
                        nextFrontier.add(next);
                    }
                }
                for (BlockPos parent : path.getParent(pos)) {
                    if (result.add(parent)) {
                        nextFrontier.add(parent);
                    }
                }
            }
            frontier = nextFrontier;
            if (frontier.isEmpty()) break;
        }
        return result;
    }
}
