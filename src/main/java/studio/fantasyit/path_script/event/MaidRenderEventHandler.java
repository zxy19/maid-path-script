package studio.fantasyit.path_script.event;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.state.EntityMaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import studio.fantasyit.path_script.PathScript;

import java.util.UUID;

@EventBusSubscriber(modid = PathScript.MODID, value = Dist.CLIENT)
public class MaidRenderEventHandler {
    private static final ContextKey<UUID> INVISIBLE_OWNER = new ContextKey<>(PathScript.id("invisible_owner"));

    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(EntityMaidRenderer.class, (EntityMaid maid, EntityMaidRenderState state) -> {
            if (maid.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
                LivingEntity owner = maid.getOwner();
                if (owner != null) {
                    state.setRenderData(INVISIBLE_OWNER, owner.getUUID());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<EntityMaid, ?, ?> event) {
        UUID ownerUuid = event.getRenderState().getRenderData(INVISIBLE_OWNER);
        if (ownerUuid != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.getUUID().equals(ownerUuid)) {
                event.setCanceled(true);
            }
        }
    }
}
