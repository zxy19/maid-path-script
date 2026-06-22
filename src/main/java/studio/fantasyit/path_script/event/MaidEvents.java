package studio.fantasyit.path_script.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidPickupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import studio.fantasyit.path_script.MaidPathScriptTask;

public class MaidEvents {
    @SubscribeEvent
    public static void onMaidPickupEntities(MaidPickupEvent.ItemResultPre event) {
        if (event.getMaid().getTask().getUid().equals(MaidPathScriptTask.ID))
            event.setCanPickup(false);
    }
}
