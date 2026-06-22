package studio.fantasyit.path_script.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import studio.fantasyit.path_script.action.BeaconAction;
import studio.fantasyit.path_script.action.IAction;

import java.util.List;

public class BeaconActionEditor {
    public static ActionEditorFactory.EditorHandle create(Screen screen, int x, int y, int width, IAction existing) {
        var defaultColor = existing instanceof BeaconAction b ? String.format("%08X", b.color()) : "FFFFFFFF";
        var defaultGlow = existing instanceof BeaconAction b ? String.format("%08X", b.glowColor()) : "20FFFFFF";
        var defaultHeight = existing instanceof BeaconAction b ? String.valueOf(b.height()) : "256";

        EditBox colorBox = new EditBox(screen.getMinecraft().font, x, y, width, 20, Component.empty());
        colorBox.setMaxLength(8);
        colorBox.setValue(defaultColor);
        colorBox.setHint(Component.literal("FFFFFFFF"));

        EditBox glowBox = new EditBox(screen.getMinecraft().font, x, y + 22, width, 20, Component.empty());
        glowBox.setMaxLength(8);
        glowBox.setValue(defaultGlow);
        glowBox.setHint(Component.literal("20FFFFFF"));

        EditBox heightBox = new EditBox(screen.getMinecraft().font, x, y + 44, width, 20, Component.empty());
        heightBox.setMaxLength(4);
        heightBox.setValue(defaultHeight);
        heightBox.setHint(Component.literal("256"));

        return new ActionEditorFactory.EditorHandle() {
            @Override
            public List<AbstractWidget> widgets() {
                return List.of(colorBox, glowBox, heightBox);
            }

            @Override
            public IAction build() {
                int color = tryParseHex(colorBox.getValue(), 0xFFFFFFFF);
                int glow = tryParseHex(glowBox.getValue(), 0x20FFFFFF);
                int height = tryParseInt(heightBox.getValue(), 256);
                return new BeaconAction(color, glow, Math.max(1, Math.min(2048, height)));
            }

            private int tryParseHex(String s, int fallback) {
                try {
                    return (int) Long.parseLong(s, 16) & 0xFFFFFFFF;
                } catch (NumberFormatException e) {
                    return fallback;
                }
            }

            private int tryParseInt(String s, int fallback) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return fallback;
                }
            }
        };
    }
}
