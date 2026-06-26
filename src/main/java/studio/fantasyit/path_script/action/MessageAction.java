package studio.fantasyit.path_script.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.util.MessageUtil;

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

    @Override
    public Component getDisplayComponent() {
        return Component.translatable("action.path_script.message", message);
    }

    @Override
    public Component getWorldDisplayComponent() {
        return Component.literal(message);
    }

    @Override
    public void onSwitchTo(Player player, EntityMaid maid, BlockPos pos) {
        player.sendSystemMessage(MessageUtil.getMaidSentChat(maid, Component.literal(message)));
    }
}
