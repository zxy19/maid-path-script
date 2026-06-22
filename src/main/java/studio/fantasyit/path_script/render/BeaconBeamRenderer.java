package studio.fantasyit.path_script.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class BeaconBeamRenderer {
    public static void submitBeaconBeam(
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            Identifier texture, float scale, float animationTime,
            int beamStart, int height,
            int solidColor, int glowColor,
            float solidRadius, float glowRadius
    ) {
        int beamEnd = beamStart + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        float scroll = height < 0 ? animationTime : -animationTime;
        float texVOff = Mth.frac(scroll * 0.2F - Mth.floor(scroll * 0.1F));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animationTime * 2.25F - 45.0F));
        float vv2 = -1.0F + texVOff;
        float vv1 = (float) height * scale * (0.5F / solidRadius) + vv2;
        submitNodeCollector.submitCustomGeometry(
                poseStack, RenderTypes.beaconBeam(texture, true),
                (pose, buffer) -> renderPart(pose, buffer,
                        solidColor, beamStart, beamEnd,
                        0.0F, solidRadius, solidRadius, 0.0F,
                        -solidRadius, 0.0F, 0.0F, -solidRadius,
                        0.0F, 1.0F, vv1, vv2)
        );
        poseStack.popPose();

        float uu1 = -1.0F + texVOff;
        float uu2 = (float) height * scale + uu1;
        submitNodeCollector.submitCustomGeometry(
                poseStack, RenderTypes.beaconBeam(texture, true),
                (pose, buffer) -> renderPart(pose, buffer,
                        glowColor, beamStart, beamEnd,
                        -glowRadius, -glowRadius, glowRadius, -glowRadius,
                        -glowRadius, glowRadius, glowRadius, glowRadius,
                        0.0F, 1.0F, uu2, uu1)
        );
        poseStack.popPose();
    }

    private static void renderPart(PoseStack.Pose pose, VertexConsumer builder, int color,
                                   int beamStart, int beamEnd,
                                   float wnx, float wnz, float enx, float enz,
                                   float wsx, float wsz, float esx, float esz,
                                   float uu1, float uu2, float vv1, float vv2) {
        renderQuad(pose, builder, color, beamStart, beamEnd, wnx, wnz, enx, enz, uu1, uu2, vv1, vv2);
        renderQuad(pose, builder, color, beamStart, beamEnd, esx, esz, wsx, wsz, uu1, uu2, vv1, vv2);
        renderQuad(pose, builder, color, beamStart, beamEnd, enx, enz, esx, esz, uu1, uu2, vv1, vv2);
        renderQuad(pose, builder, color, beamStart, beamEnd, wsx, wsz, wnx, wnz, uu1, uu2, vv1, vv2);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer builder, int color,
                                   int beamStart, int beamEnd,
                                   float wnx, float wnz, float enx, float enz,
                                   float uu1, float uu2, float vv1, float vv2) {
        addVertex(pose, builder, color, beamEnd, wnx, wnz, uu2, vv1);
        addVertex(pose, builder, color, beamStart, wnx, wnz, uu2, vv2);
        addVertex(pose, builder, color, beamStart, enx, enz, uu1, vv2);
        addVertex(pose, builder, color, beamEnd, enx, enz, uu1, vv1);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer builder, int color,
                                  int y, float x, float z, float u, float v) {
        builder.addVertex(pose, x, (float) y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
