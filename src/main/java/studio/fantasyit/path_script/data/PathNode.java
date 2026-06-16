package studio.fantasyit.path_script.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import studio.fantasyit.path_script.action.ActionManager;
import studio.fantasyit.path_script.action.IAction;

import java.util.List;

public record PathNode(BlockPos pos, List<BlockPos> next, List<IAction> actions) {
    public static final Codec<PathNode> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(PathNode::pos),
                    BlockPos.CODEC.listOf().fieldOf("next").forGetter(PathNode::next),
                    ActionManager.CODEC.listOf().fieldOf("actions").forGetter(PathNode::actions)
            ).apply(instance, PathNode::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PathNode> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PathNode::pos,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), PathNode::next,
            ActionManager.STREAM_CODEC.apply(ByteBufCodecs.list()), PathNode::actions,
            PathNode::new
    );
}
