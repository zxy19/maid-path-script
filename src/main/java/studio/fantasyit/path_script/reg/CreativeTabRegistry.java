package studio.fantasyit.path_script.reg;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.path_script.PathScript;

public class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> REGISTER
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PathScript.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PATH_SCRIPT_TAB
            = REGISTER.register("path_script_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.path_script"))
            .icon(() -> new ItemStack(ItemRegistry.PATH_EDITOR.get()))
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.PATH_EDITOR.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
