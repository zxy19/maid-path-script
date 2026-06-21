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
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.IconAction;
import studio.fantasyit.path_script.action.LabelAction;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.*;

public class MarkUtil {
    public static void updatePathMarker(EntityMaid maid, LivingEntity owner, PathSet pathSet, BlockPos currentNode) {
        if (!(owner instanceof ServerPlayer player)) return;

        PathMarker marker = player.getData(AttachmentRegistry.CLI_MARKER.get());
        MarkUtil.setupMarkerFor(marker, maid.getUUID(), pathSet);
        marker.pathingMaidEntity = maid.getUUID();
        marker.lastUpdatedNode = currentNode;
        marker.selectionPos.clear();
        marker.currentShowingTip = Component.empty();

        Path path = maid.getNavigation().createPath(currentNode, 0);
        List<BlockPos> newIndicator = new ArrayList<>();
        Set<BlockPos> fullpath = new HashSet<>();
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                fullpath.add(path.getNodePos(i));
                if (marker.pathIndicator != null && marker.pathIndicator.contains(path.getNodePos(i))) {
                    newIndicator.clear();
                } else {
                    newIndicator.add(path.getNodePos(i));
                }
            }
        }
        List<BlockPos> lastIndicator = new ArrayList<>();
        for (BlockPos pos : marker.pathIndicator) {
            lastIndicator.add(pos);
            if (fullpath.contains(pos))
                break;
        }
        marker.pathIndicatorLast = lastIndicator;
        marker.pathIndicator = newIndicator;
        player.setData(AttachmentRegistry.CLI_MARKER.get(), marker);
    }

    public static void resetMarker(LivingEntity owner) {
        if (owner.hasData(AttachmentRegistry.CLI_MARKER.get())) {
            PathMarker data = owner.getData(AttachmentRegistry.CLI_MARKER.get());
            data.selectionPos.clear();
            data.currentShowingTip = Component.empty();
            data.pathIndicator.clear();
            data.selectionPos.clear();
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
            }
        }
    }
}
