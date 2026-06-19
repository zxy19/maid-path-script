package studio.fantasyit.path_script.render;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientMarkerRender {
    private static final float[] COLOR_MARKER = new float[]{0.10f, 0.46f, 0.82f, 1};
    private static final float[] COLOR_NULL = new float[]{};

    @SubscribeEvent
    public static void onRender(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PathMarker marker = mc.player.getExistingDataOrNull(AttachmentRegistry.CLI_MARKER.get());
        if (marker == null) return;

        var poseStack = event.getPoseStack();
        var submitNodeCollector = event.getSubmitNodeCollector();
        var camera = event.getLevelRenderState().cameraRenderState;
        Map<BlockPos, Integer> floating = new ConcurrentHashMap<>();

        for (Pair<Component, BlockPos> pair : marker.tip) {
            Component text = pair.getFirst();
            BlockPos pos = pair.getSecond();
            BoxRenderUtil.renderStorage(pos, COLOR_NULL, poseStack, submitNodeCollector, camera, text, floating);
        }
        for (BlockPos pos : marker.selectionPos) {
            BoxRenderUtil.renderStorage(pos, COLOR_MARKER, poseStack, submitNodeCollector, camera, Component.translatable("path.next_step"), floating);
        }

        if (!marker.currentShowingTip.getString().isEmpty() && marker.pathingMaidEntity != null && mc.level.getEntity(marker.pathingMaidEntity) instanceof EntityMaid maid) {
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
    }


    @SubscribeEvent
    public static void levelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        PathMarker marker = mc.player.getExistingDataOrNull(AttachmentRegistry.CLI_MARKER.get());
        if (marker == null) return;

        long mills = Util.getMillis();
        if ((mills / 50) % 2 != 0) return;
        renderPath(marker.pathIndicator, mills, mc, 0x99FFCC);
        renderPath(marker.pathIndicatorLast, mills, mc, 0x99FFCC);

        for (BlockPos pos : marker.selectionPos) {
            renderPath(marker.lastUpdatedNode, pos, mills, mc, 0xFFFF99);
        }
    }

    private static void renderPath(List<BlockPos> path, long mills, Minecraft mc, int color) {
        BlockPos last = null;
        for (BlockPos node : path) {
            if (last != null) {
                renderPath(last, node, mills, mc, color);
            }
            last = node;
        }
    }

    private static void renderPath(BlockPos last, BlockPos next, long mills, Minecraft mc, int color) {
        Vec3 c1 = last.getCenter();
        Vec3 c2 = next.getCenter();
        Vec3 delta = c2.subtract(c1);
        double distance = delta.length();
        double start = (double) ((mills / 100) % 10) * 0.1;
        for (double a = start; a < distance; a += 1) {
            Vec3 p1 = c1.add(delta.normalize().scale(a));
            mc.level.addParticle(new DustParticleOptions(color, 0.6f), p1.x, p1.y, p1.z, 0.0D, 0.0D, 0.0D);
        }
    }
}
