package studio.fantasyit.path_script.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import studio.fantasyit.path_script.PathScript;

public record SoundAction(Identifier soundId, float volume, float pitch) implements IAction {
    public static Identifier ID = PathScript.id("sound");

    public static MapCodec<SoundAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound_id").forGetter(SoundAction::soundId),
            Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(SoundAction::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(SoundAction::pitch)
    ).apply(instance, SoundAction::new));

    public static StreamCodec<RegistryFriendlyByteBuf, SoundAction> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SoundAction::soundId,
            ByteBufCodecs.FLOAT, SoundAction::volume,
            ByteBufCodecs.FLOAT, SoundAction::pitch,
            SoundAction::new
    );

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Component getDisplayComponent() {
        return Component.translatable("action.path_script.sound", soundId.toString());
    }

    @Override
    public void onSwitchTo(Player player, EntityMaid maid, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundStopSoundPacket(null, SoundSource.VOICE));
            SoundEvent sound = SoundEvent.createVariableRangeEvent(soundId);
            Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
            serverPlayer.connection.send(
                    new ClientboundSoundPacket(holder, SoundSource.VOICE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            volume, pitch, player.level().getRandom().nextLong())
            );
        }
    }
}
