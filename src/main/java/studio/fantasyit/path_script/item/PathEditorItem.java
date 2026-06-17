package studio.fantasyit.path_script.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import studio.fantasyit.path_script.MaidPathScriptTask;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.memory.MemoryUtil;
import studio.fantasyit.path_script.network.NetworkHandler;
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
        Player player = context.getPlayer();

        BlockPos currentPos = stack.get(DataComponentRegistry.CURRENT_POS.get());
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());

        if (player != null && player.isShiftKeyDown()) {
            if (pathSet == null) {
                player.sendSystemMessage(Component.translatable("item.path_script.path_editor.no_path"));
                return InteractionResult.FAIL;
            }
            if (currentPos == null) {
                player.sendSystemMessage(Component.translatable("item.path_script.path_editor.no_node_selected"));
                return InteractionResult.FAIL;
            }
            if (!pathSet.contains(targetPos)) {
                player.sendSystemMessage(Component.translatable("item.path_script.path_editor.not_a_node", targetPos.toShortString()));
                return InteractionResult.FAIL;
            }
            if (currentPos.equals(targetPos)) {
                player.sendSystemMessage(Component.translatable("item.path_script.path_editor.cannot_connect_self"));
                return InteractionResult.FAIL;
            }
            if (pathSet.isAncestor(targetPos, currentPos)) {
                player.sendSystemMessage(Component.translatable("item.path_script.path_editor.would_create_cycle"));
                return InteractionResult.FAIL;
            }
            pathSet = pathSet.addEdge(currentPos, targetPos);
            stack.set(DataComponentRegistry.PATH_SET.get(), pathSet);
            stack.set(DataComponentRegistry.CURRENT_POS.get(), targetPos);
            player.sendSystemMessage(
                    Component.translatable("item.path_script.path_editor.edge_connected",
                            currentPos.toShortString(), targetPos.toShortString())
            );
            return InteractionResult.SUCCESS;
        }

        if (pathSet != null && pathSet.contains(targetPos)) {
            if (targetPos.equals(currentPos) && context.getPlayer() instanceof ServerPlayer sp) {
                NetworkHandler.sendOpenNodeEdit(sp, targetPos, pathSet);
                return InteractionResult.SUCCESS;
            }
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

        if (player != null) {
            player.sendSystemMessage(
                    Component.translatable("item.path_script.path_editor.selected", targetPos.toShortString())
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(target instanceof EntityMaid maid)) {
            return InteractionResult.PASS;
        }

        if (maid.getOwner() != player) {
            player.sendSystemMessage(Component.translatable("item.path_script.path_editor.not_owner"));
            return InteractionResult.FAIL;
        }

        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());
        if (pathSet == null) {
            player.sendSystemMessage(Component.translatable("item.path_script.path_editor.no_path"));
            return InteractionResult.FAIL;
        }

        MemoryUtil.setPathSet(maid, pathSet);
        MemoryUtil.setCurrentNode(maid, pathSet.getStartPos());
        maid.getTaskManager().setTask(new MaidPathScriptTask());

        maid.teleportTo(
                pathSet.getStartPos().getX() + 0.5,
                pathSet.getStartPos().getY(),
                pathSet.getStartPos().getZ() + 0.5
        );
        maid.getNavigationManager().resetNavigation();

        player.sendSystemMessage(
                Component.translatable("item.path_script.path_editor.maid_set",
                        maid.getDisplayName(),
                        pathSet.getStartPos().toShortString())
        );

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
