package studio.fantasyit.path_script.render;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

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

        for (Pair<ItemStack, BlockPos> pair : marker.markers) {
            ItemStack stack = pair.getFirst();
            BlockPos pos = pair.getSecond();
            Component label = stack.getDisplayName();
            BoxRenderUtil.renderStorage(pos, COLOR_NULL, poseStack, submitNodeCollector, camera, label, floating);
        }

        for (Pair<Component, BlockPos> pair : marker.tip) {
            Component text = pair.getFirst();
            BlockPos pos = pair.getSecond();
            BoxRenderUtil.renderStorage(pos, COLOR_NULL, poseStack, submitNodeCollector, camera, text, floating);
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
        BlockPos last = null;
        for (BlockPos node : marker.pathIndicator) {
            if(last != null){
                Vec3 c1 = last.getCenter();
                Vec3 c2 = node.getCenter();
                Vec3 delta = c2.subtract(c1);
                double distance = delta.length();
                double start = (double) ((mills / 100) % 10) * 0.05;
                for (double a = start; a < distance; a += 0.5) {
                    Vec3 p1 = c1.add(delta.normalize().scale(a));
                    mc.level.addParticle(new DustParticleOptions(0xaa4400, 0.6f), p1.x, p1.y, p1.z, 0.0D, 0.0D, 0.0D);
                }
            }
            last = node;
        }
    }
}
