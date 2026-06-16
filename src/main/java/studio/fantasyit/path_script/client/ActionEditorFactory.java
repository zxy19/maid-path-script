package studio.fantasyit.path_script.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import studio.fantasyit.path_script.action.IAction;

import java.util.List;

@FunctionalInterface
public interface ActionEditorFactory {

    @Nullable
    EditorHandle create(Screen screen, int x, int y, int width, IAction existing);

    interface EditorHandle {
        List<AbstractWidget> widgets();

        IAction build();
    }
}
