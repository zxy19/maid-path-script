package studio.fantasyit.path_script.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BeamRenderData(BlockPos pos, int color, int glowColor, int height) {
    public static StreamCodec<RegistryFriendlyByteBuf, BeamRenderData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BeamRenderData::pos,
            ByteBufCodecs.INT, BeamRenderData::color,
            ByteBufCodecs.INT, BeamRenderData::glowColor,
            ByteBufCodecs.INT, BeamRenderData::height,
            BeamRenderData::new
    );
}
