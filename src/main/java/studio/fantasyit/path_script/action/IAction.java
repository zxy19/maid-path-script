package studio.fantasyit.path_script.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public interface IAction {
    Identifier getId();

    Component getDisplayComponent();
}
