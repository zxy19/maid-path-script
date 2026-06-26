package studio.fantasyit.path_script.crafting;

import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.item.ItemSmartSlab;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import studio.fantasyit.path_script.item.GuideSignItem;
import studio.fantasyit.path_script.item.PathEditorItem;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.reg.RecipeRegistry;

public class GuideSignTransferRecipe implements CraftingRecipe {
    public static final MapCodec<GuideSignTransferRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Ingredient.CODEC.fieldOf("source").forGetter(r -> r.source),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
            ).apply(instance, GuideSignTransferRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GuideSignTransferRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            t -> t.source,
            ItemStackTemplate.STREAM_CODEC,
            t -> t.result,
            GuideSignTransferRecipe::new
    );

    public static final RecipeSerializer<GuideSignTransferRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final Ingredient source;
    private final ItemStackTemplate result;

    public GuideSignTransferRecipe(Ingredient source, ItemStackTemplate result) {
        this.source = source;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack guideSignStack = ItemStack.EMPTY;
        ItemStack sourceStack = ItemStack.EMPTY;

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof GuideSignItem) {
                if (!guideSignStack.isEmpty()) {
                    return false;
                }
                guideSignStack = stack;
            } else if (this.source.test(stack) && hasSourceComponent(stack)) {
                if (!sourceStack.isEmpty()) {
                    return false;
                }
                sourceStack = stack;
            } else {
                return false;
            }
        }

        return !guideSignStack.isEmpty() && !sourceStack.isEmpty();
    }

    private static boolean hasSourceComponent(ItemStack stack) {
        if (stack.getItem() instanceof PathEditorItem) {
            return stack.has(DataComponentRegistry.PATH_SET.get());
        }
        if (stack.getItem() instanceof ItemSmartSlab) {
            return stack.has(InitDataComponent.MAID_INFO.get());
        }
        if (stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            return content != null && !content.pages().isEmpty();
        }
        if (stack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            return content != null && !content.pages().isEmpty();
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack guideSignStack = ItemStack.EMPTY;
        ItemStack sourceStack = ItemStack.EMPTY;

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof GuideSignItem) {
                guideSignStack = stack;
            } else if (this.source.test(stack)) {
                sourceStack = stack;
            }
        }

        ItemStack resultStack = guideSignStack.copy();

        if (sourceStack.getItem() instanceof PathEditorItem) {
            var pathSet = sourceStack.get(DataComponentRegistry.PATH_SET.get());
            if (pathSet != null) {
                resultStack.set(DataComponentRegistry.PATH_SET.get(), pathSet);
            }
        } else if (sourceStack.getItem() instanceof ItemSmartSlab) {
            var maidInfo = sourceStack.get(InitDataComponent.MAID_INFO.get());
            if (maidInfo != null) {
                resultStack.set(DataComponentRegistry.STORED_MAID.get(), maidInfo);
            }
        } else if (sourceStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            WrittenBookContent content = sourceStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content != null && !content.pages().isEmpty()) {
                resultStack.set(DataComponentRegistry.WELCOME_MESSAGE.get(), content.pages().getFirst().raw().getString());
            }
        } else if (sourceStack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            WritableBookContent content = sourceStack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content != null && !content.pages().isEmpty()) {
                resultStack.set(DataComponentRegistry.WELCOME_MESSAGE.get(), content.pages().getFirst().raw());
            }
        }

        return resultStack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && this.source.test(stack)) {
                remaining.set(i, stack.copy());
            }
        }

        return remaining;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<GuideSignTransferRecipe> getSerializer() {
        return RecipeRegistry.GUIDE_SIGN_TRANSFER_SERIALIZER.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
