package studio.fantasyit.path_script.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class MessageUtil {
    public static Component getMaidSentChat(EntityMaid maid, Component component) {
        return Component.translatable("message.path_script.action_message",
                Component.translatable("message.path_script.maid_name_wrapper",
                        maid.getDisplayName()
                        ).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN),
                component
        );
    }
}
