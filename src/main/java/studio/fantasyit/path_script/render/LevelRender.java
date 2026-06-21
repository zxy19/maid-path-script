package studio.fantasyit.path_script.render;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.ItemRegistry;

import java.util.List;
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
//        renderDebug(poseStack, submitNodeCollector, camera, partialTick, mc, floating);
    }

    private static void renderDebug(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        List<Entity> entities = mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(32));
        for (Entity entity : entities) {
            if (entity instanceof EntityMaid maid) {
                if (maid.hasHome())
                    BoxRenderUtil.renderStorage(maid.getHomePosition(), colors_r, poseStack, submitNodeCollector, camera, Component.literal("home"), floating);
                maid.getBrain().getMemory(MemoryModuleType.PATH).ifPresent(t -> {
                    for (int i = 0; i < t.getNodeCount(); i++) {
                        BoxRenderUtil.renderStorage(t.getNodePos(i), colors_g, poseStack, submitNodeCollector, camera, Component.literal("node[" + i + "]"), floating);
                    }
                });
            }
        }
    }


    private static void renderForRequest(com.mojang.blaze3d.vertex.PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, float partialTick, Minecraft mc, Map<BlockPos, Integer> floating) {
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.PATH_EDITOR.get()) {
            return;
        }
        PathSet data = mainStack.get(DataComponentRegistry.PATH_SET);
        BlockPos currentPos = mainStack.get(DataComponentRegistry.CURRENT_POS.get());
        if (data != null) {
            for (PathNode node : data.getNodes()) {
                boolean isCurrent = node.pos().equals(currentPos);
                float[] color = isCurrent ? colors_y : colors_b;
                Component label = Component.translatable("tip.path_script.node", node.pos().toShortString());
                BoxRenderUtil.renderStorage(node.pos(), color, poseStack, submitNodeCollector, camera, label, floating);

                if (isCurrent) {
                    Component currentLabel = Component.translatable("tip.path_script.current");
                    Vec3 from = node.pos().getCenter().add(0, 0.7f, 0);
                    BoxRenderUtil.drawText(poseStack, mc, camera, submitNodeCollector, from, currentLabel, 0xffffff00, floating.getOrDefault(node.pos(), 0) * 0.3f);
                    floating.put(node.pos(), floating.getOrDefault(node.pos(), 0) + 1);
                }

                for (IAction action : node.actions()) {
                    Component actionLabel = action.getWorldDisplayComponent();
                    Vec3 from = node.pos().getCenter().add(0, 0.7f, 0);
                    BoxRenderUtil.drawText(poseStack, mc, camera, submitNodeCollector, from, actionLabel, 0xffaaaaaa, floating.getOrDefault(node.pos(), 0) * 0.3f);
                    floating.put(node.pos(), floating.getOrDefault(node.pos(), 0) + 1);
                }
            }
        }
    }
}
