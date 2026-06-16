package studio.fantasyit.path_script.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import studio.fantasyit.path_script.action.IAction;
import studio.fantasyit.path_script.data.PathNode;
import studio.fantasyit.path_script.data.PathSet;
import studio.fantasyit.path_script.memory.MemoryUtil;
import studio.fantasyit.path_script.reg.MemoryModuleRegistry;

import java.util.List;
import java.util.Map;

public class MaidFollowPathMoveBehavior extends Behavior<EntityMaid> {
    private static final double TELEPORT_THRESHOLD_SQR = 32.0 * 32.0;
    private static final float SPEED_MODIFIER = 0.5f;
    private static final int CLOSE_ENOUGH_DIST = 2;

    public MaidFollowPathMoveBehavior() {
        super(Map.of(
                MemoryModuleRegistry.CURRENT_PATH_SCRIPT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleRegistry.CURRENT_NODE.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        return owner != null && maid.level() == owner.level();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long timestamp) {
        PathSet pathSet = MemoryUtil.getPathSet(maid).orElse(null);
        if (pathSet == null) return;

        BlockPos currentNode = MemoryUtil.getCurrentNode(maid).orElse(null);
        if (currentNode == null) {
            currentNode = pathSet.getStartPos();
            MemoryUtil.setCurrentNode(maid, currentNode);
        }

        setWalkTarget(maid, currentNode);
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long timestamp) {
        PathSet pathSet = MemoryUtil.getPathSet(maid).orElse(null);
        if (pathSet == null) return;

        Player player = (Player) maid.getOwner();
        if (player == null) return;

        BlockPos maidPos = maid.blockPosition();
        BlockPos playerPos = player.blockPosition();

        PathNode maidNearest = pathSet.getNearest(maidPos);
        PathNode playerNearest = pathSet.getNearest(playerPos);

        if (maidNearest == null || playerNearest == null) return;

        if (playerPos.distSqr(playerNearest.pos()) > TELEPORT_THRESHOLD_SQR) {
            teleportMaid(maid, maidNearest.pos());
            return;
        }

        if (pathSet.isAncestor(playerNearest.pos(), maidNearest.pos())) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            return;
        }

        if (!pathSet.isAncestor(maidNearest.pos(), playerNearest.pos())) {
            teleportMaid(maid, playerNearest.pos());
            MemoryUtil.setCurrentNode(maid, playerNearest.pos());
            setWalkTarget(maid, playerNearest.pos());
            return;
        }

        BlockPos currentNode = MemoryUtil.getCurrentNode(maid).orElse(null);
        if (currentNode == null) return;

        if (Math.abs(maidPos.getX() - currentNode.getX()) <= CLOSE_ENOUGH_DIST
                && Math.abs(maidPos.getY() - currentNode.getY()) <= CLOSE_ENOUGH_DIST
                && Math.abs(maidPos.getZ() - currentNode.getZ()) <= CLOSE_ENOUGH_DIST) {
            executeActions(player, maid, currentNode, pathSet);

            List<BlockPos> nextPositions = pathSet.getNext(currentNode);
            if (!nextPositions.isEmpty()) {
                BlockPos nextNode = nextPositions.get(0);
                MemoryUtil.setCurrentNode(maid, nextNode);
                setWalkTarget(maid, nextNode);
            }
        } else {
            if (maid.getBrain().getMemory(MemoryModuleType.WALK_TARGET).isEmpty()) {
                setWalkTarget(maid, currentNode);
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long timestamp) {
        return MemoryUtil.getPathSet(maid).isPresent();
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long timestamp) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private void setWalkTarget(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, SPEED_MODIFIER, CLOSE_ENOUGH_DIST));
    }

    private void teleportMaid(EntityMaid maid, BlockPos pos) {
        maid.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        maid.getNavigationManager().resetNavigation();
    }

    private void executeActions(Player player, EntityMaid maid, BlockPos pos, PathSet pathSet) {
        for (IAction action : pathSet.getAction(pos)) {
            action.onSwitchTo(player, maid, pos);
        }
    }
}
