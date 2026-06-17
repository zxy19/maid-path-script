package studio.fantasyit.path_script.data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PathMarker {
    public @Nullable UUID pathingMaidEntity;
    public List<BlockPos> pathIndicator;
    public List<BlockPos> pathIndicatorLast;
    public List<Pair<ItemStack, BlockPos>> markers;
    public List<Pair<Component, BlockPos>> tip;

    private static final StreamCodec<RegistryFriendlyByteBuf, Pair<ItemStack, BlockPos>> MARKER_STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Pair::getFirst,
            BlockPos.STREAM_CODEC, Pair::getSecond,
            Pair::of
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, Pair<Component, BlockPos>> TIP_STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, Pair::getFirst,
            BlockPos.STREAM_CODEC, Pair::getSecond,
            Pair::of
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PathMarker> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional),
            m -> Optional.ofNullable(m.pathingMaidEntity),
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.pathIndicator,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.pathIndicatorLast,
            MARKER_STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.markers,
            TIP_STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.tip,
            (uuid, blocks, blocksLast, markers, tips) -> {
                var pm = new PathMarker();
                pm.pathingMaidEntity = uuid.orElse(null);
                pm.pathIndicator = blocks;
                pm.pathIndicatorLast = blocksLast;
                pm.markers = markers;
                pm.tip = tips;
                return pm;
            }
    );

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.pathingMaidEntity);
        hash = 97 * hash + Objects.hashCode(this.pathIndicator);
        hash = 97 * hash + Objects.hashCode(this.pathIndicatorLast);
        hash = 97 * hash + Objects.hashCode(this.markers);
        hash = 97 * hash + Objects.hashCode(this.tip);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PathMarker other = (PathMarker) obj;
        if (!Objects.equals(this.pathingMaidEntity, other.pathingMaidEntity)) return false;
        if (!Objects.equals(this.pathIndicator, other.pathIndicator)) return false;
        if (!Objects.equals(this.pathIndicatorLast, other.pathIndicatorLast)) return false;
        if (!Objects.equals(this.markers, other.markers)) return false;
        if (!Objects.equals(this.tip, other.tip)) return false;
        return true;
    }
}