package studio.fantasyit.path_script.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.item.GuideSignItem;
import studio.fantasyit.path_script.item.PathEditorItem;
import studio.fantasyit.path_script.reg.DataComponentRegistry;

public class AnvilHandler {
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof GuideSignItem && right.getItem() instanceof PathEditorItem) {
            PathSet pathSet = right.get(DataComponentRegistry.PATH_SET.get());
            if (pathSet != null && !left.has(DataComponentRegistry.PATH_SET.get())) {
                ItemStack output = left.copy();
                output.set(DataComponentRegistry.PATH_SET.get(), pathSet);
                event.setOutput(output);
                event.setXpCost(1);
                event.setMaterialCost(0);
            }
        }
    }
}
