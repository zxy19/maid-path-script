package studio.fantasyit.path_script.action;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

public interface IAction {
    Identifier getId();
}
