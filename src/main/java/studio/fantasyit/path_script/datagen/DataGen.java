package studio.fantasyit.path_script.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.reg.ItemRegistry;

@EventBusSubscriber(modid = PathScript.MODID, value = Dist.CLIENT)
public class DataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider((output) -> new ModelProvider(output, PathScript.MODID) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                itemModels.generateFlatItem(ItemRegistry.PATH_EDITOR.get(), Items.PAPER, ModelTemplates.FLAT_ITEM);
                itemModels.generateFlatItem(ItemRegistry.GUIDE_SIGN.get(), Items.BOOK, ModelTemplates.FLAT_ITEM);
            }
        });
    }
}
