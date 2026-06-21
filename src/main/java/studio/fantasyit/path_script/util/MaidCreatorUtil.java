package studio.fantasyit.path_script.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.UUID;

public class MaidCreatorUtil {
    public static void loadMaid(EntityMaid maid, CustomData storedMaid, Player owner) {
        CompoundTag tag = storedMaid.copyTag();
        tag.store("UUID", UUIDUtil.CODEC, UUID.randomUUID());
        tag.store("Owner", UUIDUtil.CODEC, owner.getUUID());
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, owner.registryAccess(), tag);
        maid.load(input);
    }
}
