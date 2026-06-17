package studio.fantasyit.path_script.reg;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.item.GuideSignItem;
import studio.fantasyit.path_script.item.PathEditorItem;

public class ItemRegistry {
    public static final DeferredRegister<Item> REGISTER
            = DeferredRegister.create(Registries.ITEM, PathScript.MODID);

    public static final DeferredHolder<Item, PathEditorItem> PATH_EDITOR
            = REGISTER.register("path_editor", PathEditorItem::new);

    public static final DeferredHolder<Item, GuideSignItem> GUIDE_SIGN
            = REGISTER.register("guide_sign", GuideSignItem::new);

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
