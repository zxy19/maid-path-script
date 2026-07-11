package studio.fantasyit.path_script.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import studio.fantasyit.path_script.PathScript;

public class RenderUtil {
    private static final Identifier WAY_ARROW_TEXTURE = Identifier.fromNamespaceAndPath(PathScript.MODID, "textures/particle/way.png");
    private static final RenderType WAY_ARROW_RENDER_TYPE = RenderTypes.entityCutoutCull(WAY_ARROW_TEXTURE);
    public static void renderWaypointArrowsForEdge(BlockPos from, BlockPos to, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,int color) {
        Vec3 c1 = from.getCenter();
        Vec3 c2 = to.getCenter();
        Vec3 delta = c2.subtract(c1);
        double distance = delta.length();
        if (distance < 0.01) return;

        long mills = Util.getMillis();
        double start = (double) ((mills / 100) % 10) * 0.02;
        Vec3 dir = delta.normalize();

        Vector3f worldDir = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);
        Quaternionf invCam = new Quaternionf(camera.orientation).invert();
        Vector3f screenDir = worldDir.rotate(invCam);
        float angle = -(float) Math.toDegrees(Math.atan2(screenDir.x, screenDir.y));

        for (double a = start; a < distance; a += 0.2) {
            Vec3 pos = c1.add(dir.scale(a));
            poseStack.pushPose();
            poseStack.translate(pos.x - camera.pos.x, pos.y - camera.pos.y, pos.z - camera.pos.z);
            poseStack.mulPose(camera.orientation);
            poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            poseStack.scale(0.15f, 0.15f, 0.15f);
            collector.submitCustomGeometry(poseStack, WAY_ARROW_RENDER_TYPE, (pose, buffer) -> {
                buffer.addVertex(pose, -0.5f, -0.5f, 0).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 1, 0);
                buffer.addVertex(pose, 0.5f, -0.5f, 0).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 1, 0);
                buffer.addVertex(pose, 0.5f, 0.5f, 0).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 1, 0);
                buffer.addVertex(pose, -0.5f, 0.5f, 0).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 1, 0);
            });
            poseStack.popPose();
        }
    }
}
