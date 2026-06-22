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

import java.util.*;

public class PathMarker {
    public @Nullable UUID pathingMaidEntity;
    public BlockPos lastUpdatedNode = BlockPos.ZERO;
    public List<BlockPos> pathIndicator = new ArrayList<>();
    public List<BlockPos> pathIndicatorLast = new ArrayList<>();

    public Component currentShowingTip = Component.empty();
    public List<BlockPos> selectionPos = new ArrayList<>();

    public List<Pair<Component, BlockPos>> tip = new ArrayList<>();
    public List<Pair<List<ItemStack>, BlockPos>> icons = new ArrayList<>();
    public List<BeamRenderData> beams = new ArrayList<>();

    private static final StreamCodec<RegistryFriendlyByteBuf, Pair<Component, BlockPos>> TIP_STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, Pair::getFirst,
            BlockPos.STREAM_CODEC, Pair::getSecond,
            Pair::of
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, Pair<List<ItemStack>, BlockPos>> ICON_STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), Pair::getFirst,
            BlockPos.STREAM_CODEC, Pair::getSecond,
            Pair::of
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PathMarker> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            m -> m.lastUpdatedNode,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional),
            m -> Optional.ofNullable(m.pathingMaidEntity),
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.pathIndicator,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.pathIndicatorLast,
            ComponentSerialization.STREAM_CODEC,
            m -> m.currentShowingTip,
            TIP_STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.tip,
            ICON_STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.icons,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.selectionPos,
            BeamRenderData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            m -> m.beams,
            (lastUpdated, uuid, blocks, blocksLast, component, tips, icons, selection, beams) -> {
                var pm = new PathMarker();
                pm.lastUpdatedNode = lastUpdated;
                pm.pathingMaidEntity = uuid.orElse(null);
                pm.pathIndicator = blocks;
                pm.pathIndicatorLast = blocksLast;
                pm.tip = tips;
                pm.icons = icons;
                pm.currentShowingTip = component;
                pm.selectionPos = selection;
                pm.beams = beams;
                return pm;
            }
    );

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.pathingMaidEntity);
        hash = 97 * hash + Objects.hashCode(this.pathIndicator);
        hash = 97 * hash + Objects.hashCode(this.pathIndicatorLast);
        hash = 97 * hash + Objects.hashCode(this.tip);
        hash = 97 * hash + Objects.hashCode(this.currentShowingTip);
        hash = 97 * hash + Objects.hashCode(this.selectionPos);
        hash = 97 * hash + Objects.hashCode(this.icons);
        hash = 97 * hash + Objects.hashCode(this.beams);
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
        if (!Objects.equals(this.currentShowingTip, other.currentShowingTip)) return false;
        if (!Objects.equals(this.tip, other.tip)) return false;
        if (!Objects.equals(this.selectionPos, other.selectionPos)) return false;
        if (!Objects.equals(this.icons, other.icons)) return false;
        if (!Objects.equals(this.beams, other.beams)) return false;
        return true;
    }
}