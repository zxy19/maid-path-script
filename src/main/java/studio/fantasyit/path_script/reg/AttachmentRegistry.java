package studio.fantasyit.path_script.reg;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.data.PathMarker;

import java.util.function.Supplier;

public class AttachmentRegistry {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PathScript.MODID);

    public static final Supplier<AttachmentType<PathMarker>> CLI_MARKER = ATTACHMENT_TYPES.register(
            "client_render_mark", () -> AttachmentType.builder(PathMarker::new)
                    .sync(PathMarker.STREAM_CODEC)
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}