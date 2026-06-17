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

public record LabelAction(String message) implements IAction {
    public static Identifier ID = PathScript.id("label");
    public static MapCodec<LabelAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("message").forGetter(LabelAction::message)
    ).apply(instance, LabelAction::new));
    public static StreamCodec<RegistryFriendlyByteBuf, LabelAction> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            LabelAction::message,
            LabelAction::new
    );

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Component getDisplayComponent() {
        return Component.translatable("action.path_script.label", message);
    }

    @Override
    public void onSwitchTo(Player player, EntityMaid maid, BlockPos pos) {
        player.sendSystemMessage(Component.literal(message));
    }
}
