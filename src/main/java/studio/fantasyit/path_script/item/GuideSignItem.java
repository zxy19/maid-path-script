package studio.fantasyit.path_script.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
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
import studio.fantasyit.path_script.reg.AttachmentRegistry;
import studio.fantasyit.path_script.reg.DataComponentRegistry;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class GuideSignItem extends Item {
    public GuideSignItem(Identifier id) {
        super(new Properties().stacksTo(1).setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), id)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        return use(context.getLevel(), player, context.getHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());
        if (pathSet == null) {
            player.sendSystemMessage(Component.translatable("item.path_script.guide_sign.no_path"));
            return InteractionResult.FAIL;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        Optional<UUID> existingUuid = player.getData(AttachmentRegistry.GUIDE_MAID.get());
        existingUuid.ifPresent(uuid -> {
            var entity = serverLevel.getEntity(uuid);
            if (entity instanceof EntityMaid maid && maid.isAlive()) {
                maid.discard();
            }
        });

        EntityMaid maid = EntityMaid.TYPE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (maid == null) {
            return InteractionResult.FAIL;
        }

        maid.tame(player);
        maid.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);

        MemoryUtil.setPathSet(maid, pathSet);
        MemoryUtil.setCurrentNode(maid, pathSet.getStartPos());
        maid.getTaskManager().setTask(new MaidPathScriptTask());

        maid.setPos(pathSet.getStartPos().getX() + 0.5, pathSet.getStartPos().getY(), pathSet.getStartPos().getZ() + 0.5);
        serverLevel.addFreshEntity(maid);
        maid.getNavigationManager().resetNavigation();

        player.setData(AttachmentRegistry.GUIDE_MAID.get(), Optional.of(maid.getUUID()));

        player.sendSystemMessage(Component.translatable("item.path_script.guide_sign.maid_created", pathSet.getStartPos().toShortString()));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        PathSet pathSet = stack.get(DataComponentRegistry.PATH_SET.get());
        if (pathSet != null) {
            builder.accept(Component.translatable("item.path_script.path_editor.node_count", pathSet.getNodes().size()));
        }
    }
}
