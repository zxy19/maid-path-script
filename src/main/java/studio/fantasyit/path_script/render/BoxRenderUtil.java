package studio.fantasyit.path_script.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class BoxRenderUtil {
    public static void renderStorage(BlockPos storage, float[] colors, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Component key, Map<BlockPos, Integer> floating) {
        renderStorage(storage, colors, poseStack, submitNodeCollector, camera, key, floating, 0xffffffff);
    }

    public static void renderStorage(BlockPos storage, float[] colors, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Component key, Map<BlockPos, Integer> floating, int textColor) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 position = camera.pos.reverse();
        AABB aabb = new AABB(storage).move(position);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.LINES, (poseState, consumer) -> {
            renderBox(poseStack, consumer, aabb, colors[0], colors[1], colors[2], colors[3]);
        });
        if (!key.getString().isEmpty()) {
            Vec3 livingFrom = storage.getCenter().add(0, 0.7f, 0);
            drawText(poseStack, mc, camera, submitNodeCollector, livingFrom, key, textColor, floating.getOrDefault(storage, 0) * 0.3f);
            floating.put(storage, floating.getOrDefault(storage, 0) + 1);
        }
    }

    public static void drawText(PoseStack poseStack, Minecraft mc, CameraRenderState camera, SubmitNodeCollector submitNodeCollector, Vec3 livingFrom, Component key, int textColor, float floatingTransform) {
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 fromPos = mc.player.getEyePosition(partialTick);
        Vec3 posFromPlayer = fromPos.vectorTo(livingFrom);
        poseStack.pushPose();
        poseStack.translate(posFromPlayer.x, posFromPlayer.y, posFromPlayer.z);
        poseStack.translate(0, floatingTransform, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot));
        poseStack.scale(-0.025f, -0.025f, -1f);
        poseStack.translate(-mc.font.width(key) / 2f, 0, 0);
        submitNodeCollector.submitText(poseStack, 0, 0,
                key.getVisualOrderText(),
                mc.font.isBidirectional(),
                Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT, textColor, 0, 0);
        poseStack.popPose();
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, AABB aabb, float r, float g, float b, float a) {
        VoxelShape shape = Shapes.create(aabb);
        int color = ARGB.color((int) (a * 255), (int) (r * 255), (int) (g * 255), (int) (b * 255));
        ShapeRenderer.renderShape(poseStack, consumer, shape, 0, 0, 0, color, 2.0f);
    }
}
