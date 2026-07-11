package studio.fantasyit.path_script.render;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.path_script.Config;
import studio.fantasyit.path_script.data.BeamRenderData;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.ItemRegistry;
import studio.fantasyit.path_script.util.MarkUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientMarkerRender {
    private static final float[] COLOR_MARKER = new float[]{0.10f, 0.46f, 0.82f, 1};
    private static final float[] COLOR_NULL = new float[]{};

    @SubscribeEvent
    public static void onRender(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var poseStack = event.getPoseStack();
        var submitNodeCollector = event.getSubmitNodeCollector();
        var camera = event.getLevelRenderState().cameraRenderState;
        Map<BlockPos, Integer> floating = new ConcurrentHashMap<>();

        PathMarker marker = mc.player.getExistingDataOrNull(AttachmentRegistry.CLI_MARKER.get());
        ItemStack mainHandItem = mc.player.getMainHandItem();
        PathSet pathSet;
        if (mainHandItem.is(ItemRegistry.GUIDE_SIGN) && mainHandItem.has(DataComponentRegistry.PATH_SET)) {
            pathSet = mainHandItem.get(DataComponentRegistry.PATH_SET);
        } else {
            pathSet = null;
        }
        boolean showMarks = true;
        if (mainHandItem.is(ItemRegistry.GUIDE_SIGN) && mainHandItem.has(DataComponentRegistry.PATH_SET) && pathSet != null) {
            PathMarker pm = new PathMarker();
            MarkUtil.setupMarkerFor(pm, pathSet);
            renderLabelAndMarks(pm, mc.player, poseStack, submitNodeCollector, camera, floating);
            renderWaypointArrowsForPathSet(pathSet, mc.player, poseStack, submitNodeCollector, camera, 0xffB1FBA4, marker != null ? marker.historyNodes : null);
            showMarks = false;
        }
        if (marker == null) return;

        if (showMarks)
            renderLabelAndMarks(marker, mc.player, poseStack, submitNodeCollector, camera, floating);
        for (BlockPos pos : marker.selectionPos) {
            BoxRenderUtil.renderStorage(pos, COLOR_MARKER, poseStack, submitNodeCollector, camera, Component.translatable("path.next_step"), floating);
        }

        if (!marker.currentShowingTip.getString().isEmpty() && marker.pathingMaidEntity != null && mc.level.getEntity(marker.pathingMaidEntity) instanceof EntityMaid maid) {
            renderCurrentShowingTipText(maid, poseStack, mc, camera, marker, submitNodeCollector);
        }
        renderWaypointArrowsForPath(marker.pathIndicator, poseStack, submitNodeCollector, camera, 0xffffffff);
        for (BlockPos pos : marker.selectionPos) {
            RenderUtil.renderWaypointArrowsForEdge(marker.lastUpdatedNode, pos, poseStack, submitNodeCollector, camera, 0xff0175EC);
        }
    }

    private static void renderCurrentShowingTipText(EntityMaid maid, PoseStack poseStack, Minecraft mc, CameraRenderState camera, PathMarker marker, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 fromPos = mc.player.getEyePosition(partialTick);
        Vec3 posFromPlayer = fromPos.vectorTo(maid.position());
        poseStack.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
        poseStack.translate(0, maid.getEyeHeight() / 2, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot));
        poseStack.translate(0, 0, -0.4);
        poseStack.scale(-0.025f, -0.025f, -1f);
        poseStack.translate(-mc.font.width(marker.currentShowingTip) / 2f, 0, 0);
        submitNodeCollector.submitText(poseStack, 0, 0,
                marker.currentShowingTip.getVisualOrderText(),
                mc.font.isBidirectional(),
                Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT, 0xffffffff, 0, 0);
        poseStack.popPose();
    }

    private static void renderLabelAndMarks(PathMarker marker, LocalPlayer player, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Map<BlockPos, Integer> floating) {
        for (Pair<Component, BlockPos> pair : marker.tip) {
            Component text = pair.getFirst();
            BlockPos pos = pair.getSecond();
            if (marker.selectionPos.contains(pos) || pos.distToCenterSqr(player.position()) < Config.distanceToShowMarks * Config.distanceToShowMarks) {
                BoxRenderUtil.renderStorage(pos, COLOR_NULL, poseStack, submitNodeCollector, camera, text, floating);
            }
        }

        for (Pair<List<ItemStack>, BlockPos> pair : marker.icons) {
            List<ItemStack> icons = pair.getFirst();
            if (icons.isEmpty()) continue;
            ItemStack icon = icons.get((player.tickCount / 10) % icons.size());
            BlockPos pos = pair.getSecond();
            if (marker.selectionPos.contains(pos) || pos.distToCenterSqr(player.position()) < Config.distanceToShowMarks * Config.distanceToShowMarks) {
                poseStack.pushPose();
                poseStack.translate(pos.getCenter().subtract(camera.pos));
                poseStack.translate(0, 0.7f + floating.getOrDefault(pos, 0) * 0.3f, 0);
                poseStack.scale(0.4f, 0.4f, 0.4f);
                poseStack.mulPose(Axis.YP.rotationDegrees(player.tickCount * 3));
                int lightCoords = LevelRenderer.getLightCoords(player.level(), pos);
                ItemStackRenderState state = new ItemStackRenderState();
                Minecraft.getInstance().getItemModelResolver().appendItemLayers(state, icon, ItemDisplayContext.NONE, player.level(), player, 0);
                state.submit(poseStack, submitNodeCollector, lightCoords, 0, 0);
                poseStack.popPose();
                floating.put(pos, floating.getOrDefault(pos, 0) + 2);
            }
        }

        for (BeamRenderData beam : marker.beams) {
            if (marker.selectionPos.contains(beam.pos()) || beam.pos().distToCenterSqr(player.position()) < Config.distanceToShowMarks * Config.distanceToShowMarks) {
                float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
                float animationTime = (player.level().getGameTime() % 40) + partialTick;
                Vec3 offset = Vec3.atLowerCornerOf(beam.pos()).subtract(camera.pos);
                poseStack.pushPose();
                poseStack.translate(offset.x, offset.y, offset.z);
                BeaconBeamRenderer.submitBeaconBeam(poseStack, submitNodeCollector,
                        BeaconRenderer.BEAM_LOCATION, 1.0F, animationTime,
                        0, beam.height(),
                        beam.color(), beam.glowColor(),
                        0.2F, 0.25F);
                poseStack.popPose();
            }
        }
    }

    private static void renderWaypointArrowsForPathSet(PathSet data, LocalPlayer player, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, int color, @Nullable List<BlockPos> skipNodes) {
        double distSq = Config.distanceToShowMarks * Config.distanceToShowMarks;
        Set<BlockPos> ign = new HashSet<>();
        if (skipNodes != null) {
            for (int i = skipNodes.size() - 1, c = 0; i >= 0 && c < 4; i--, c++) {
                ign.add(skipNodes.get(i));
            }
        }
        Vec3 playerPos = player.position();
        for (PathNode node : data.getNodes()) {
            if (node.pos().distToCenterSqr(playerPos) < distSq) {
                for (BlockPos next : node.next()) {
                    if (ign.contains(next) && ign.contains(node.pos())) continue;
                    RenderUtil.renderWaypointArrowsForEdge(node.pos(), next, poseStack, collector, camera, color);
                }
            }
        }
    }

    private static void renderWaypointArrowsForPath(List<BlockPos> path, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, int color) {
        BlockPos last = null;
        for (BlockPos node : path) {
            if (last != null) {
                RenderUtil.renderWaypointArrowsForEdge(last, node, poseStack, collector, camera, color);
            }
            last = node;
        }
    }
}
