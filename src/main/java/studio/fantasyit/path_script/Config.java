package studio.fantasyit.path_script;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = PathScript.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue TELEPORT_MAX_DISTANCE = BUILDER
            .comment("Max distance (blocks) between maid and owner before teleporting maid to nearest path node")
            .defineInRange("teleport.max_distance", 24.0, 1.0, 1024.0);

    private static final ModConfigSpec.DoubleValue TELEPORT_CLEAR_DISTANCE = BUILDER
            .comment("If owner is farther than this distance from ALL path nodes, clear the path data instead of teleporting")
            .defineInRange("teleport.clear_distance", 10.0, 1.0, 1024.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static double teleportMaxDistance;
    public static double teleportClearDistance;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        teleportMaxDistance = TELEPORT_MAX_DISTANCE.get();
        teleportClearDistance = TELEPORT_CLEAR_DISTANCE.get();
    }
}
