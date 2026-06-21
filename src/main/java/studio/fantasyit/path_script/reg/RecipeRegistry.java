package studio.fantasyit.path_script.reg;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.crafting.GuideSignTransferRecipe;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS
            = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PathScript.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GuideSignTransferRecipe>> GUIDE_SIGN_TRANSFER_SERIALIZER
            = RECIPE_SERIALIZERS.register("guide_sign_transfer", () -> GuideSignTransferRecipe.SERIALIZER);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
