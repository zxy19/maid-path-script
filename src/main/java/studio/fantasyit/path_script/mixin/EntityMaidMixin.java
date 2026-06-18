package studio.fantasyit.path_script.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import studio.fantasyit.path_script.PathScript;

@Mixin(EntityMaid.class)
public abstract class EntityMaidMixin extends TamableAnimal {

    protected EntityMaidMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            return player != self.getOwner();
        }
        return super.isInvisibleTo(player);
    }

    @Override
    public boolean isPushable() {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            return false;
        }
        return super.isPushable();
    }

    @Override
    protected void pushEntities() {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            return;
        }
        super.pushEntities();
    }
}
