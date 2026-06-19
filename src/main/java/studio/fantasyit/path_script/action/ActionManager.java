package studio.fantasyit.path_script.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ActionManager {
    static Map<Identifier, MapCodec<IAction>> CODECS = new HashMap<>();
    static Map<Identifier, StreamCodec<RegistryFriendlyByteBuf, IAction>> STREAMCODECS = new HashMap<>();
    static Map<Identifier, Component> DISPLAY_COMPONENTS = new HashMap<>();

    public static Codec<IAction> CODEC = Identifier.CODEC.dispatch("id", IAction::getId, id -> CODECS.get(id));
    public static StreamCodec<RegistryFriendlyByteBuf, IAction> STREAM_CODEC = StreamCodec.of((t, a) -> {
        t.writeIdentifier(a.getId());
        STREAMCODECS.get(a.getId()).encode(t, a);
    }, t -> {
        Identifier id = t.readIdentifier();
        return STREAMCODECS.get(id).decode(t);
    });

    @SuppressWarnings("unchecked")
    public static void register(Identifier id, MapCodec<? extends IAction> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends IAction> streamCodec, Component displayComponent) {
        CODECS.put(id, (MapCodec<IAction>) codec);
        STREAMCODECS.put(id, (StreamCodec<RegistryFriendlyByteBuf, IAction>) streamCodec);
        DISPLAY_COMPONENTS.put(id, displayComponent);
    }

    public static Component getDisplayComponent(Identifier id) {
        return DISPLAY_COMPONENTS.getOrDefault(id, Component.literal(id.toString()));
    }

    public static void init() {
        register(MessageAction.ID, MessageAction.CODEC, MessageAction.STREAM_CODEC, Component.translatable("action.path_script.message"));
        register(LabelAction.ID, LabelAction.CODEC, LabelAction.STREAM_CODEC, Component.translatable("action.path_script.label"));
        register(IconAction.ID, IconAction.CODEC, IconAction.STREAM_CODEC, Component.translatable("action.path_script.icon"));
        register(SoundAction.ID, SoundAction.CODEC, SoundAction.STREAM_CODEC, Component.translatable("action.path_script.sound"));
    }
}
