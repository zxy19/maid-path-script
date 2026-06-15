package studio.fantasyit.path_script.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.ItemRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT)
public class LevelRender {
    private static final float[] colors_g = new float[]{0.40f, 0.73f, 0.42f, 1};
    private static final float[] colors_r = new float[]{0.91f, 0.12f, 0.39f, 1};
    private static final float[] colors_b = new float[]{0.10f, 0.46f, 0.82f, 1};
    private static final float[] colors_y = new float[]{0.91f, 0.73f, 0.0f, 1};
    private static final float[] colors_p = new float[]{0.37f, 0.21f, 0.69f, 1};
    private static final float[][] colors = new float[][]{colors_b, colors_g, colors_y};

    @SubscribeEvent
    public static void onRender(SubmitCustomGeometryEvent event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        var poseStack = event.getPoseStack();
        var submitNodeCollector = event.getSubmitNodeCollector();
        var camera = event.getLevelRenderState().cameraRenderState;
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Map<BlockPos, Integer> floating = new ConcurrentHashMap<>();
        renderForRequest(poseStack, submitNodeCollector, camera, partialTick, mc, floating);

    }


    private static void renderForRequest(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.PATH_EDITOR.get()) {
            return;
        }
        PathSet data = mainStack.get(DataComponentRegistry.PATH_SET);
        if (data != null) {
            for (PathNode i : data.getNodes()) {
                BoxRenderUtil.renderStorage(i.pos(), colors_b, poseStack, submitNodeCollector, camera, Component.translatable("tip.path_script.node", i.toString()), floating);
            }
        }
    }
}
