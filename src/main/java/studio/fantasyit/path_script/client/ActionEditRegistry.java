package studio.fantasyit.path_script.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.jspecify.annotations.Nullable;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.action.ActionManager;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.action.MessageAction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@EventBusSubscriber(value = Dist.CLIENT, modid = PathScript.MODID)
public class ActionEditRegistry {
    private static final Map<Identifier, Entry> ENTRIES = new HashMap<>();

    public static void register(Identifier id, Supplier<IAction> defaultFactory,
                                 @Nullable ActionEditorFactory editorFactory) {
        ENTRIES.put(id, new Entry(defaultFactory, editorFactory));
    }

    public static Set<Identifier> getIds() {
        return new LinkedHashSet<>(ENTRIES.keySet());
    }

    public static IAction createDefault(Identifier id) {
        Entry entry = ENTRIES.get(id);
        if (entry == null) return null;
        return entry.defaultFactory.get();
    }

    @Nullable
    public static ActionEditorFactory getEditor(Identifier id) {
        Entry entry = ENTRIES.get(id);
        return entry != null ? entry.editorFactory : null;
    }

    public static Component getDisplayName(Identifier id) {
        return ActionManager.getDisplayComponent(id);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        register(MessageAction.ID, () -> new MessageAction(""), MessageActionEditor::create);
    }

    private record Entry(Supplier<IAction> defaultFactory, @Nullable ActionEditorFactory editorFactory) {
    }
}
