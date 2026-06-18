package studio.fantasyit.path_script.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.EventBusSubscriber;
import studio.fantasyit.path_script.action.MessageAction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.item.PathEditorItem;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
@EventBusSubscriber
public class PathEditorCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("patheditor")
                        .then(Commands.literal("delete")
                                .executes(PathEditorCommand::deleteNode)
                        )
                        .then(Commands.literal("action")
                                .then(Commands.literal("message")
                                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                                .executes(PathEditorCommand::addMessageAction)
                                        )
                                )
                        )
        );
    }

    private static int deleteNode(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof PathEditorItem)) {
            source.sendFailure(Component.translatable("command.path_script.not_holding_editor"));
            return 0;
        }

        BlockPos currentPos = stack.get(DataComponentRegistry.CURRENT_POS.get());
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());

        if (currentPos == null || pathSet == null) {
            source.sendFailure(Component.translatable("command.path_script.no_node_selected"));
            return 0;
        }

        if (currentPos.equals(pathSet.getStartPos())) {
            source.sendFailure(Component.translatable("command.path_script.cannot_delete_start"));
            return 0;
        }

        PathSet newPathSet = pathSet.removeNode(currentPos);
        if (newPathSet == null) {
            stack.remove(DataComponentRegistry.PATH_SET.get());
            stack.remove(DataComponentRegistry.CURRENT_POS.get());
        } else {
            stack.set(DataComponentRegistry.PATH_SET.get(), newPathSet);
            stack.set(DataComponentRegistry.CURRENT_POS.get(), newPathSet.getStartPos());
        }

        source.sendSuccess(() -> Component.translatable("command.path_script.node_deleted", currentPos.toShortString()), true);
        return 1;
    }

    private static int addMessageAction(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Player player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof PathEditorItem)) {
            source.sendFailure(Component.translatable("command.path_script.not_holding_editor"));
            return 0;
        }

        BlockPos currentPos = stack.get(DataComponentRegistry.CURRENT_POS.get());
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());

        if (currentPos == null || pathSet == null) {
            source.sendFailure(Component.translatable("command.path_script.no_node_selected"));
            return 0;
        }

        String text = StringArgumentType.getString(ctx, "text");

        PathSet newPathSet = pathSet.addAction(currentPos, new MessageAction(text));
        stack.set(DataComponentRegistry.PATH_SET.get(), newPathSet);

        source.sendSuccess(() -> Component.translatable("command.path_script.action_added", text, currentPos.toShortString()), true);
        return 1;
    }
}
