package studio.fantasyit.path_script.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.path_script.PathScript;

import java.util.List;

public record IconAction(List<ItemStack> markers) implements IAction {
    public static Identifier ID = PathScript.id("marker");

    public static MapCodec<IconAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.listOf().fieldOf("icon").forGetter(IconAction::markers)
    ).apply(instance, IconAction::new));

    public static StreamCodec<RegistryFriendlyByteBuf, IconAction> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), IconAction::markers,
            IconAction::new
    );

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Component getDisplayComponent() {
        long l = Util.getMillis() / 1000;
        Component d = Component.literal("?");
        if (!markers.isEmpty())
            d = markers.get((int) (l % markers.size())).getHoverName();
        return Component.translatable("action.path_script.icon", d);
    }
}
