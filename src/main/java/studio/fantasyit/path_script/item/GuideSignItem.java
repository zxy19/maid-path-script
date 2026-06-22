package studio.fantasyit.path_script.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import studio.fantasyit.path_script.behavior.BehaviorAndConditions;
import studio.fantasyit.path_script.data.PathMarker;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.reg.AttachmentRegistry;
import studio.fantasyit.path_script.reg.DataComponentRegistry;
import studio.fantasyit.path_script.util.MaidCreatorUtil;
import studio.fantasyit.path_script.util.MarkUtil;

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
        if (existingUuid.isPresent()) {
            existingUuid.ifPresent(uuid -> {
                var entity = serverLevel.getEntity(uuid);
                if (entity instanceof EntityMaid maid && maid.isAlive()) {
                    maid.discard();
                }
            });
            player.setData(AttachmentRegistry.GUIDE_MAID, Optional.empty());
            MarkUtil.clearMarker(player);
        } else {
            CustomData storedMaid = stack.get(DataComponentRegistry.STORED_MAID.get());
            EntityMaid maid = EntityMaid.TYPE.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (maid == null) {
                return InteractionResult.FAIL;
            }
            maid.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(player.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
            if (storedMaid != null) {
                MaidCreatorUtil.loadMaid(maid, storedMaid, player);
            }
            level.addFreshEntity(maid);
            level.getServer().schedule(new TickTask(1, () -> {
                player.setData(AttachmentRegistry.GUIDE_MAID.get(), Optional.of(maid.getUUID()));
                BehaviorAndConditions.setUpMaidForPath(maid, pathSet, player);
                PathMarker data = player.getData(AttachmentRegistry.CLI_MARKER.get());
                data.pathingMaidEntity = null;
                MarkUtil.setupMarkerFor(data, maid.getUUID(), pathSet);
                player.setData(AttachmentRegistry.CLI_MARKER, data);
            }));
        }
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
