package studio.fantasyit.path_script.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import studio.fantasyit.path_script.PathScript;

public record BeaconAction(int color, int glowColor, int height) implements IAction {
    public static Identifier ID = PathScript.id("beacon");

    public static MapCodec<BeaconAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter(BeaconAction::color),
            Codec.INT.optionalFieldOf("glow_color", 0x20FFFFFF).forGetter(BeaconAction::glowColor),
            Codec.INT.optionalFieldOf("height", 256).forGetter(BeaconAction::height)
    ).apply(instance, BeaconAction::new));

    public static StreamCodec<RegistryFriendlyByteBuf, BeaconAction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, BeaconAction::color,
            ByteBufCodecs.INT, BeaconAction::glowColor,
            ByteBufCodecs.INT, BeaconAction::height,
            BeaconAction::new
    );

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Component getDisplayComponent() {
        return Component.translatable("action.path_script.beacon", String.format("#%08X", color));
    }
}
