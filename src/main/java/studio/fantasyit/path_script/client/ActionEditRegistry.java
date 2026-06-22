package studio.fantasyit.path_script.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.jspecify.annotations.Nullable;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.action.*;

import java.util.*;
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
        register(IconAction.ID, () -> new IconAction(new ArrayList<>()), IconActionEditor::create);
        register(LabelAction.ID, () -> new LabelAction(""), LabelActionEditor::create);
        register(SoundAction.ID, () -> new SoundAction(Identifier.fromNamespaceAndPath("minecraft", "entity.experience_orb.pickup"), 1.0F, 1.0F), SoundActionEditor::create);
        register(BeaconAction.ID, () -> new BeaconAction(0xFFFFFFFF, 0x20FFFFFF, 256), BeaconActionEditor::create);
    }

    private record Entry(Supplier<IAction> defaultFactory, @Nullable ActionEditorFactory editorFactory) {
    }
}
