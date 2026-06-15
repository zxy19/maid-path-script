package studio.fantasyit.path_script.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.DataComponentRegistry;

import java.util.List;
import java.util.function.Consumer;

public class PathEditorItem extends Item {
    public PathEditorItem(Identifier id) {
        super(new Properties().stacksTo(1).setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), id)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockPos targetPos = clickedPos.relative(context.getClickedFace());
        ItemStack stack = context.getItemInHand();

        BlockPos currentPos = stack.get(DataComponentRegistry.CURRENT_POS.get());
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());

        if (pathSet != null && pathSet.contains(targetPos)) {
            stack.set(DataComponentRegistry.CURRENT_POS.get(), targetPos);
        } else {
            if (pathSet == null) {
                pathSet = new PathSet(targetPos, List.of());
            } else {
                pathSet = pathSet.addNode(currentPos, targetPos);
            }
            stack.set(DataComponentRegistry.PATH_SET.get(), pathSet);
            stack.set(DataComponentRegistry.CURRENT_POS.get(), targetPos);
        }

        Player player = context.getPlayer();
        if (player != null) {
            player.sendSystemMessage(
                    Component.translatable("item.path_script.path_editor.selected", targetPos.toShortString())
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        BlockPos currentPos = stack.get(DataComponentRegistry.CURRENT_POS.get());
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());

        if (currentPos != null) {
            builder.accept(Component.translatable("item.path_script.path_editor.current_pos", currentPos.toShortString()));
        }
        if (pathSet != null) {
            builder.accept(Component.translatable("item.path_script.path_editor.node_count", pathSet.getNodes().size()));
        }
    }
}
