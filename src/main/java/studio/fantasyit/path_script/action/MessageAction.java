package studio.fantasyit.path_script.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import studio.fantasyit.path_script.PathScript;

public record MessageAction(String message) implements IAction {
    public static Identifier ID = PathScript.id("message");
    public static MapCodec<MessageAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("message").forGetter(MessageAction::message)
    ).apply(instance, MessageAction::new));
    public static StreamCodec<RegistryFriendlyByteBuf, MessageAction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            MessageAction::message,
            MessageAction::new
    );

    @Override
    public Identifier getId() {
        return ID;
    }
}
