package studio.fantasyit.path_script;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import net.neoforged.neoforge.common.NeoForge;
import studio.fantasyit.path_script.action.ActionManager;
import studio.fantasyit.path_script.command.PathEditorCommand;
import studio.fantasyit.path_script.reg.CreativeTabRegistry;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.ItemRegistry;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

@Mod(PathScript.MODID)
public class PathScript {
    public static final String MODID = "path_script";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public PathScript(IEventBus modEventBus, ModContainer modContainer) {
        MemoryModuleRegistry.REGISTER.register(modEventBus);
        DataComponentRegistry.REGISTER.register(modEventBus);
        ItemRegistry.REGISTER.register(modEventBus);
        CreativeTabRegistry.REGISTER.register(modEventBus);
        ActionManager.init();
        NeoForge.EVENT_BUS.register(PathEditorCommand.class);
    }
}
