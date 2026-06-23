package studio.fantasyit.path_script;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import studio.fantasyit.path_script.behavior.*;
import studio.fantasyit.path_script.reg.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public class MaidPathScriptTask implements IMaidTask {
    public static final Identifier ID = PathScript.id("path_navigate");

    @Override
    public Identifier getUid() {
        return ID;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ItemRegistry.PATH_EDITOR.get());
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return null;
    }

    @Override
    public boolean canSitInJoy(EntityMaid maid, String joyType) {
        return false;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return new ArrayList<>(List.of(
                Pair.of(0, new MaidTeleportToOwnerBehavior()),
                Pair.of(1, new MaidWaitOwnerBehavior()),
                Pair.of(1, new MaidContinueMoveBehavior()),
                Pair.of(2, new MaidSwitchPathNode()),
                Pair.of(2, new MaidWaitBeforeMultipleSelector()),
                Pair.of(3, new MaidLeavePathModeBehavior()),
                Pair.of(4, new MaidWaitOnPathEnd()),
                Pair.of(5, new MaidWatchOwnerOnWaiting())
        ));
    }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        return new AABB(maid.blockPosition()).inflate(64, VERTICAL_SEARCH_RANGE, 64);
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return false;
    }

    @Override
    public boolean enablePanic(EntityMaid maid) {
        return false;
    }
}
