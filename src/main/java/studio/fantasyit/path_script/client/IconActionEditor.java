package studio.fantasyit.path_script.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.IconAction;

import java.util.ArrayList;
import java.util.List;

public class IconActionEditor {
    private static final int CELL = 18;
    private static final int GAP = 2;
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 4;
    private static final int SELECTED_H = CELL;
    private static final int GRID_TOP = SELECTED_H + GAP;
    private static final int TOTAL_H = GRID_TOP + GRID_ROWS * CELL;

    public static ActionEditorFactory.EditorHandle create(Screen screen, int x, int y, int width, IAction existing) {
        List<ItemStack> initial = existing instanceof IconAction icon ? new ArrayList<>(icon.markers()) : new ArrayList<>();
        IconPickerWidget widget = new IconPickerWidget(0, 0, width, initial);

        return new ActionEditorFactory.EditorHandle() {
            @Override
            public List<AbstractWidget> widgets() {
                return List.of(widget);
            }

            @Override
            public IAction build() {
                return new IconAction(new ArrayList<>(widget.getSelectedItems()));
            }
        };
    }

    private static class IconPickerWidget extends AbstractWidget {
        private final List<ItemStack> selectedItems;

        IconPickerWidget(int x, int y, int width, List<ItemStack> initial) {
            super(x, y, width, TOTAL_H, Component.empty());
            this.selectedItems = initial;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            Font font = Minecraft.getInstance().font;

            graphics.fill(getX(), getY(), getX() + width, getY() + SELECTED_H, 0x22FFFFFF);

            for (int i = 0; i < selectedItems.size(); i++) {
                int ix = getX() + i * CELL;
                if (ix + CELL > getX() + width) break;
                ItemStack stack = selectedItems.get(i);
                graphics.item(stack, ix, getY());
                graphics.itemDecorations(font, stack, ix, getY());
                if (mouseX >= ix && mouseX < ix + CELL && mouseY >= getY() && mouseY < getY() + CELL) {
                    graphics.fill(ix, getY(), ix + CELL, getY() + CELL, 0x80FFFFFF);
                    graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                }
            }

            var inventory = Minecraft.getInstance().player.getInventory();
            int gridY = getY() + GRID_TOP;
            int slotIndex = 0;
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    int ix = getX() + col * CELL;
                    int iy = gridY + row * CELL;
                    ItemStack stack = inventory.getItem(slotIndex++);
                    if (stack.isEmpty()) continue;

                    graphics.item(stack, ix, iy);
                    graphics.itemDecorations(font, stack, ix, iy);
                    if (mouseX >= ix && mouseX < ix + CELL && mouseY >= iy && mouseY < iy + CELL) {
                        graphics.fill(ix, iy, ix + CELL, iy + CELL, 0x80FFFFFF);
                        graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                    }
                }
            }
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            int mx = (int) event.x();
            int my = (int) event.y();

            if (my >= getY() && my < getY() + SELECTED_H) {
                int idx = (mx - getX()) / CELL;
                if (idx >= 0 && idx < selectedItems.size() && mx < getX() + (idx + 1) * CELL) {
                    selectedItems.remove(idx);
                    return;
                }
            }

            int gridY = getY() + GRID_TOP;
            if (my >= gridY && my < gridY + GRID_ROWS * CELL) {
                int col = (mx - getX()) / CELL;
                int row = (my - gridY) / CELL;
                if (col >= 0 && col < GRID_COLS && row >= 0 && row < GRID_ROWS) {
                    int slotIndex = row * GRID_COLS + col;
                    var inventory = Minecraft.getInstance().player.getInventory();
                    if (slotIndex < inventory.getContainerSize()) {
                        ItemStack stack = inventory.getItem(slotIndex);
                        if (!stack.isEmpty()) {
                            selectedItems.add(stack.copy());
                        }
                    }
                }
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, Component.translatable("action.path_script.icon"));
        }

        List<ItemStack> getSelectedItems() {
            return selectedItems;
        }
    }
}
