package studio.fantasyit.path_script.action;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public interface IAction {
    Identifier getId();

    Component getDisplayComponent();

    default Component getWorldDisplayComponent() {
        return getDisplayComponent();
    }

    default void onSwitchTo(Player player, EntityMaid maid, BlockPos pos) {
    }
}
