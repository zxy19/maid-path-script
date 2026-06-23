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
            .defineInRange("teleport.max_distance", 20, 1.0, 1024.0);

    private static final ModConfigSpec.DoubleValue TELEPORT_CLEAR_DISTANCE = BUILDER
            .comment("If owner is farther than this distance from ALL path nodes, clear the path data instead of teleporting")
            .defineInRange("teleport.clear_distance", 10.0, 1.0, 1024.0);

    private static final ModConfigSpec.DoubleValue TELEPORT_Y_MAX_HEIGHT = BUILDER
            .comment("Maximum Y difference to consider a node. Nodes beyond this vertical distance are ignored.")
            .defineInRange("teleport.y_max_height", 4.0, 1.0, 256.0);

    private static final ModConfigSpec.DoubleValue TELEPORT_Y_POSITIVE_WEIGHT = BUILDER
            .comment("Weight for Y difference when the node is above the player. Higher value makes nodes above harder to select.")
            .defineInRange("teleport.y_positive_weight", 5.0, 1.0, 100.0);

    private static final ModConfigSpec.DoubleValue TELEPORT_Y_NEGATIVE_WEIGHT = BUILDER
            .comment("Weight for Y difference when the node is below the player. Lower value makes nodes below easier to select.")
            .defineInRange("teleport.y_negative_weight", 3.0, 0.1, 100.0);
    private static final ModConfigSpec.DoubleValue NODE_DIST_WEIGHT = BUILDER
            .comment("Weight for node distance")
            .defineInRange("teleport.node_dist_weight", 5.0, 0.1, 100.0);

    private static final ModConfigSpec.DoubleValue DISTANCE_TO_SHOW_MARKS = BUILDER
            .comment("Distance to show marks")
            .defineInRange("distance to show marks", 64.0, 1.0, 1024.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static double teleportMaxDistance;
    public static double clearDistance;
    public static double yMaxHeight;
    public static double yPositiveWeight;
    public static double yNegativeWeight;
    public static double nodeDistWeight;
    public static double distanceToShowMarks;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        teleportMaxDistance = TELEPORT_MAX_DISTANCE.get();
        clearDistance = TELEPORT_CLEAR_DISTANCE.get();
        yMaxHeight = TELEPORT_Y_MAX_HEIGHT.get();
        yPositiveWeight = TELEPORT_Y_POSITIVE_WEIGHT.get();
        yNegativeWeight = TELEPORT_Y_NEGATIVE_WEIGHT.get();
        nodeDistWeight = NODE_DIST_WEIGHT.get();
        distanceToShowMarks = DISTANCE_TO_SHOW_MARKS.get();
    }
}
