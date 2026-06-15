package studio.fantasyit.path_script.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.ItemRegistry;

@EventBusSubscriber(value = Dist.CLIENT)
public class LevelTickEvent {
    @SubscribeEvent
    public static void levelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack mainStack = mc.player.getMainHandItem();
        if (mainStack.getItem() != ItemRegistry.PATH_EDITOR.get()) return;
        PathSet data = mainStack.get(DataComponentRegistry.PATH_SET);
        if (data == null) return;
        if (mc.player.tickCount % 5 != 0) return;
        for (PathNode node : data.getNodes()) {
            BlockPos cur = node.pos();
            for (BlockPos next : node.next()) {
                Vec3 c1 = cur.getCenter();
                Vec3 c2 = next.getCenter();
                Vec3 delta = c2.subtract(c1);
                double distance = delta.length();
                double start = (double) ((mc.player.tickCount / 10) % 5) * 0.1;
                for (double a = start; a < distance; a += 0.5) {
                    Vec3 p1 = c1.add(delta.normalize().scale(a));
                    mc.level.addParticle(new DustParticleOptions(0xaa4400, 0.6f), p1.x, p1.y, p1.z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}
