package studio.fantasyit.path_script.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Path;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.action.BeaconAction;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.IconAction;
import studio.fantasyit.path_script.action.LabelAction;
import studio.fantasyit.path_script.data.BeamRenderData;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarkUtil {
    public static void updatePathMarker(EntityMaid maid, LivingEntity owner, PathSet pathSet, BlockPos currentNode) {
        if (!(owner instanceof ServerPlayer player)) return;

        PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
        MarkUtil.setupMarkerFor(marker, maid.getUUID(), pathSet);
        marker.pathingMaidEntity = maid.getUUID();
        marker.historyNodes.add(currentNode);
        marker.lastUpdatedNode = currentNode;
        marker.selectionPos.clear();
        marker.currentShowingTip = Component.empty();

        Path path = maid.getNavigation().createPath(currentNode, 0);
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                BlockPos nodePos = path.getNodePos(i);
                if (i <= 5) {
                    for (int j = 0; j < marker.pathIndicator.size() && j < 5; j++) {
                        BlockPos prevPos = marker.pathIndicator.get(marker.pathIndicator.size() - j - 1);
                        int dist = prevPos.distManhattan(nodePos);
                        if (dist <= 1) {
                            for (int ii = 0; ii < j; ii++)
                                marker.pathIndicator.removeLast();
                            if (dist == 0) {
                                marker.pathIndicator.removeLast();
                            }
                        }
                    }
                }
                marker.pathIndicator.add(nodePos);
            }
        }
        if (marker.pathIndicator.size() > 30)
            marker.pathIndicator = new ArrayList<>(marker.pathIndicator.subList(marker.pathIndicator.size() - 30, marker.pathIndicator.size()));
        player.setData(AttachmentRegistry.CLI_MARKER.get(), marker);
    }

    public static void resetMarker(LivingEntity owner) {
        if (owner.hasData(AttachmentRegistry.CLI_MARKER.get())) {
            PathMarker data = owner.getData(AttachmentRegistry.CLI_MARKER.get());
            data.selectionPos.clear();
            data.currentShowingTip = Component.empty();
            data.pathIndicator.clear();
            data.selectionPos.clear();
            data.historyNodes.clear();
        }
    }

    public static void clearMarker(LivingEntity owner) {
        owner.removeData(AttachmentRegistry.CLI_MARKER.get());
    }

    public static void clearClientMarkerIfInvalid(ServerPlayer player, ServerLevel level) {
        PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
        if (marker.pathingMaidEntity == null) return;

        Entity entity = level.getEntity(marker.pathingMaidEntity);
        if (!(entity instanceof EntityMaid maid) || !maid.isAlive()) {
            clearMarker(player);
            return;
        }
        if (!maid.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            clearMarker(player);
        }
    }

    public static void setupMarkerFor(PathMarker marker, UUID uuid, PathSet path) {
        if (marker.pathingMaidEntity == null || !marker.pathingMaidEntity.equals(uuid)) {
            marker.tip.clear();
            marker.icons.clear();
            marker.beams.clear();
            setupMarkerFor(marker, path);
            marker.pathingMaidEntity = uuid;
        }
    }

    public static void setupMarkerFor(PathMarker marker, PathSet path) {
        for (PathNode n : path.getNodes()) {
            for (IAction a : n.actions()) {
                if (a instanceof LabelAction(String message))
                    marker.tip.add(new Pair<>(Component.literal(message), n.pos()));
                if (a instanceof IconAction(List<net.minecraft.world.item.ItemStack> markers))
                    marker.icons.add(new Pair<>(markers, n.pos()));
                if (a instanceof BeaconAction(var color, var glowColor, var height))
                    marker.beams.add(new BeamRenderData(n.pos(), color, glowColor, height));
            }
        }
    }
}
