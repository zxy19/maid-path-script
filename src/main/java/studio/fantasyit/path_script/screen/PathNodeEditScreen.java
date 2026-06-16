package studio.fantasyit.path_script.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.client.ActionEditRegistry;
import studio.fantasyit.path_script.client.ActionEditorFactory;
import studio.fantasyit.path_script.network.UpdateNodePacket;

import java.util.ArrayList;
import java.util.List;

public class PathNodeEditScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int PADDING = 8;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 35;
    private static final int BOTTOM_BAR_Y_OFFSET = 36;

    private final BlockPos pos;
    private final List<ActionRow> actionRows;
    private final List<Identifier> actionTypeIds;

    private int leftX;

    private boolean addingAction;
    private int addActionTypeIndex;
    private IAction addActionDraft;
    private ActionEditorFactory.@org.jspecify.annotations.Nullable EditorHandle addEditorHandle;

    private boolean deleteNodeConfirm;

    public PathNodeEditScreen(BlockPos pos, List<IAction> actions) {
        super(Component.translatable("screen.path_script.edit_node", pos.toShortString()));
        this.pos = pos;
        this.actionRows = new ArrayList<>();
        for (IAction action : actions) {
            this.actionRows.add(new ActionRow(action));
        }
        this.actionTypeIds = new ArrayList<>(ActionEditRegistry.getIds());
    }

    @Override
    protected void init() {
        this.leftX = (this.width - WIDTH) / 2;
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        int y = LIST_TOP;

        for (int i = 0; i < actionRows.size(); i++) {
            ActionRow row = actionRows.get(i);
            final int idx = i;

            if (row.editing) {
                y = buildEditRow(y, row.action, row.editorHandle,
                        a -> { row.action = a; row.editing = false; row.editorHandle = null; rebuildWidgets(); },
                        () -> { row.editing = false; row.editorHandle = null; rebuildWidgets(); });
            } else {
                y = buildDisplayRow(y, row.action,
                        () -> {
                            ActionEditorFactory editor = ActionEditRegistry.getEditor(row.action.getId());
                            if (editor != null) {
                                row.editorHandle = editor.create(this, 0, 0, WIDTH - PADDING * 2 - 90, row.action);
                                row.editing = true;
                                rebuildWidgets();
                            }
                        },
                        () -> { actionRows.remove(idx); rebuildWidgets(); });
            }
        }

        if (addingAction) {
            y = buildAddActionRow(y);
        }

        int bottomY = this.height - BOTTOM_BAR_Y_OFFSET;

        Button addButton = Button.builder(
                        Component.translatable("screen.path_script.add_action"),
                        b -> startAddAction())
                .pos(leftX + PADDING, bottomY)
                .size(100, 20)
                .build();
        addRenderableWidget(addButton);

        Button deleteButton = Button.builder(
                        deleteNodeConfirm
                                ? Component.translatable("screen.path_script.confirm_delete_node")
                                : Component.translatable("screen.path_script.delete_node"),
                        b -> {
                            if (deleteNodeConfirm) {
                                ClientPacketDistributor.sendToServer(
                                        new UpdateNodePacket(pos, List.of(), true));
                                onClose();
                            } else {
                                deleteNodeConfirm = true;
                                rebuildWidgets();
                            }
                        })
                .pos(leftX + PADDING + 108, bottomY)
                .size(80, 20)
                .build();
        addRenderableWidget(deleteButton);

        Button doneButton = Button.builder(
                        CommonComponents.GUI_DONE,
                        b -> {
                            List<IAction> actions = new ArrayList<>();
                            for (ActionRow row : actionRows) {
                                actions.add(row.action);
                            }
                            ClientPacketDistributor.sendToServer(
                                    new UpdateNodePacket(pos, actions, false));
                            onClose();
                        })
                .pos(leftX + WIDTH - PADDING - 50, bottomY)
                .size(50, 20)
                .build();
        addRenderableWidget(doneButton);
    }

    private int buildDisplayRow(int y, IAction action, Runnable onEdit, Runnable onDelete) {
        boolean hasEditor = ActionEditRegistry.getEditor(action.getId()) != null;
        int btnWidth = hasEditor ? 80 : 40;
        int btnX = leftX + WIDTH - PADDING - btnWidth;

        if (hasEditor) {
            Button editButton = Button.builder(
                            Component.translatable("screen.path_script.edit"),
                            b -> onEdit.run())
                    .pos(btnX, y)
                    .size(38, ROW_HEIGHT)
                    .build();
            addRenderableWidget(editButton);
        }

        Button deleteButton = Button.builder(
                        Component.translatable("screen.path_script.delete"),
                        b -> onDelete.run())
                .pos(leftX + WIDTH - PADDING - 40, y)
                .size(38, ROW_HEIGHT)
                .build();
        addRenderableWidget(deleteButton);

        return y + ROW_HEIGHT + 2;
    }

    private int buildEditRow(int y, IAction action,
                              ActionEditorFactory.@org.jspecify.annotations.Nullable EditorHandle handle,
                              java.util.function.Consumer<IAction> onSave, Runnable onCancel) {
        if (handle == null) return y;

        List<AbstractWidget> editorWidgets = handle.widgets();
        int editorY = y;

        for (AbstractWidget w : editorWidgets) {
            w.setX(leftX + PADDING);
            w.setY(editorY);
            addRenderableWidget(w);
            if (w instanceof EditBox eb) {
                setInitialFocus(eb);
            }
            editorY += w.getHeight() + 2;
        }

        int btnY = editorY;
        Button saveButton = Button.builder(
                        CommonComponents.GUI_DONE,
                        b -> onSave.accept(handle.build()))
                .pos(leftX + WIDTH - PADDING - 80, btnY)
                .size(38, ROW_HEIGHT)
                .build();
        addRenderableWidget(saveButton);

        Button cancelButton = Button.builder(
                        CommonComponents.GUI_CANCEL,
                        b -> onCancel.run())
                .pos(leftX + WIDTH - PADDING - 40, btnY)
                .size(38, ROW_HEIGHT)
                .build();
        addRenderableWidget(cancelButton);

        return btnY + ROW_HEIGHT + 2;
    }

    private void startAddAction() {
        addingAction = true;
        addActionTypeIndex = 0;
        refreshAddActionDraft();
        rebuildWidgets();
    }

    private void refreshAddActionDraft() {
        if (actionTypeIds.isEmpty()) return;
        Identifier typeId = actionTypeIds.get(addActionTypeIndex % actionTypeIds.size());
        addActionDraft = ActionEditRegistry.createDefault(typeId);
        ActionEditorFactory editor = ActionEditRegistry.getEditor(typeId);
        if (editor != null && addActionDraft != null) {
            addEditorHandle = editor.create(this, 0, 0, WIDTH - PADDING * 2 - 90, addActionDraft);
        } else {
            addEditorHandle = null;
        }
    }

    private int buildAddActionRow(int y) {
        if (actionTypeIds.isEmpty()) return y;

        Identifier currentType = actionTypeIds.get(addActionTypeIndex % actionTypeIds.size());
        Component typeName = ActionEditRegistry.getDisplayName(currentType);

        Button typeButton = Button.builder(
                        typeName,
                        b -> {
                            addActionTypeIndex = (addActionTypeIndex + 1) % actionTypeIds.size();
                            refreshAddActionDraft();
                            rebuildWidgets();
                        })
                .pos(leftX + PADDING, y)
                .size(80, ROW_HEIGHT)
                .build();
        addRenderableWidget(typeButton);

        int editorY = y;
        if (addEditorHandle != null) {
            List<AbstractWidget> editorWidgets = addEditorHandle.widgets();
            editorY = y + ROW_HEIGHT + 2;
            for (AbstractWidget w : editorWidgets) {
                w.setX(leftX + PADDING);
                w.setY(editorY);
                addRenderableWidget(w);
                if (w instanceof EditBox eb) {
                    setInitialFocus(eb);
                }
                editorY += w.getHeight() + 2;
            }
        }

        int btnY = Math.max(y + ROW_HEIGHT, editorY);
        Button saveButton = Button.builder(
                        CommonComponents.GUI_DONE,
                        b -> {
                            IAction newAction = addEditorHandle != null
                                    ? addEditorHandle.build()
                                    : addActionDraft;
                            if (newAction != null) {
                                actionRows.add(new ActionRow(newAction));
                            }
                            addingAction = false;
                            addEditorHandle = null;
                            addActionDraft = null;
                            rebuildWidgets();
                        })
                .pos(leftX + WIDTH - PADDING - 80, btnY)
                .size(38, ROW_HEIGHT)
                .build();
        addRenderableWidget(saveButton);

        Button cancelButton = Button.builder(
                        CommonComponents.GUI_CANCEL,
                        b -> {
                            addingAction = false;
                            addEditorHandle = null;
                            addActionDraft = null;
                            rebuildWidgets();
                        })
                .pos(leftX + WIDTH - PADDING - 40, btnY)
                .size(38, ROW_HEIGHT)
                .build();
        addRenderableWidget(cancelButton);

        return btnY + ROW_HEIGHT + 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xc0101010);
        graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
        graphics.fill(leftX, LIST_TOP - 5, leftX + WIDTH, this.height - BOTTOM_BAR_Y_OFFSET + 5, 0x88000000);

        int y = LIST_TOP;
        for (ActionRow row : actionRows) {
            if (!row.editing) {
                Component display = row.action.getDisplayComponent();
                graphics.text(this.font, display, leftX + PADDING, y + 5, 0xFFAAAAAA, false);
            }
            y += ROW_HEIGHT + 2;
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ActionRow {
        IAction action;
        boolean editing;
        ActionEditorFactory.@org.jspecify.annotations.Nullable EditorHandle editorHandle;

        ActionRow(IAction action) {
            this.action = action;
        }
    }
}
