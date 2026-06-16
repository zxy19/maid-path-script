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

public record OpenNodeEditPacket(BlockPos pos, List<IAction> actions) implements CustomPacketPayload {
    public static final Type<OpenNodeEditPacket> TYPE = new Type<>(PathScript.id("open_node_edit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenNodeEditPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenNodeEditPacket::pos,
            ActionManager.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenNodeEditPacket::actions,
            OpenNodeEditPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
