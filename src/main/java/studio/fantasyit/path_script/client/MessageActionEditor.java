package studio.fantasyit.path_script.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.MessageAction;

import java.util.List;

public class MessageActionEditor {
    public static ActionEditorFactory.EditorHandle create(Screen screen, int x, int y, int width, IAction existing) {
        String initial = existing instanceof MessageAction msg ? msg.message() : "";
        EditBox editBox = new EditBox(screen.getMinecraft().font, x, y, width, 20, Component.empty());
        editBox.setMaxLength(256);
        editBox.setValue(initial);

        return new ActionEditorFactory.EditorHandle() {
            @Override
            public List<AbstractWidget> widgets() {
                return List.of(editBox);
            }

            @Override
            public IAction build() {
                return new MessageAction(editBox.getValue());
            }
        };
    }
}
