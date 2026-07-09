package studio.fantasyit.path_script.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.item.PathEditorItem;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.screen.ScreenHandler;

@EventBusSubscriber(modid = PathScript.MODID)
public class NetworkHandler {

    @SubscribeEvent
    static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PathScript.MODID);

        registrar.playToClient(
                OpenNodeEditPacket.TYPE,
                OpenNodeEditPacket.STREAM_CODEC,
                (payload, ctx) -> {
                    ScreenHandler.openEditor(payload);
                }
        );

        registrar.playToServer(
                UpdateNodePacket.TYPE,
                UpdateNodePacket.STREAM_CODEC,
                (payload, ctx) -> {
                    ServerPlayer player = (ServerPlayer) ctx.player();
                    ItemStack stack = player.getMainHandItem();

                    if (!(stack.getItem() instanceof PathEditorItem)) return;

                    BlockPos pos = payload.pos();
                    PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());

                    if (pathSet == null || !pathSet.contains(pos)) return;

                    if (payload.deleteNode()) {
                        PathSet newPathSet = pathSet.removeNode(pos);
                        if (newPathSet == null) {
                            stack.remove(DataComponentRegistry.PATH_SET.get());
                            stack.remove(DataComponentRegistry.CURRENT_POS.get());
                        } else {
                            stack.set(DataComponentRegistry.PATH_SET.get(), newPathSet);
                            stack.set(DataComponentRegistry.CURRENT_POS.get(), newPathSet.getStartPos());
                        }
                    } else {
                        PathSet newPathSet = pathSet.setActions(pos, payload.actions());
                        stack.set(DataComponentRegistry.PATH_SET.get(), newPathSet);
                    }
                }
        );
    }

    public static void sendOpenNodeEdit(ServerPlayer player, BlockPos pos, PathSet pathSet) {
        PacketDistributor.sendToPlayer(player,
                new OpenNodeEditPacket(pos, pathSet.getAction(pos)));
    }
}
