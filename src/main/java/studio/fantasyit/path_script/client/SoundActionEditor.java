package studio.fantasyit.path_script.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.SoundAction;

import java.util.List;

public class SoundActionEditor {
    public static ActionEditorFactory.EditorHandle create(Screen screen, int x, int y, int width, IAction existing) {
        String initial = existing instanceof SoundAction sound ? sound.soundId().toString() : "";
        EditBox editBox = new EditBox(screen.getMinecraft().font, x, y, width, 20, Component.empty());
        editBox.setMaxLength(256);
        editBox.setValue(initial);
        editBox.setHint(Component.literal("minecraft:entity.experience_orb.pickup"));

        return new ActionEditorFactory.EditorHandle() {
            @Override
            public List<AbstractWidget> widgets() {
                return List.of(editBox);
            }

            @Override
            public IAction build() {
                Identifier soundId = Identifier.tryParse(editBox.getValue());
                if (soundId == null) {
                    soundId = Identifier.fromNamespaceAndPath("minecraft", "entity.experience_orb.pickup");
                }
                return new SoundAction(soundId, 1.0F, 1.0F);
            }
        };
    }
}
