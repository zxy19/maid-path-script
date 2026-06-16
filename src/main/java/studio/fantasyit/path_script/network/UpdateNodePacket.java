package studio.fantasyit.path_script.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.action.ActionManager;
import studio.fantasyit.path_script.action.IAction;

import java.util.List;

public record UpdateNodePacket(BlockPos pos, List<IAction> actions, boolean deleteNode) implements CustomPacketPayload {
    public static final Type<UpdateNodePacket> TYPE = new Type<>(PathScript.id("update_node"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNodePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateNodePacket::pos,
            ActionManager.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateNodePacket::actions,
            ByteBufCodecs.BOOL, UpdateNodePacket::deleteNode,
            UpdateNodePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
