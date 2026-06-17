package studio.fantasyit.path_script.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.path_script.PathScript;

@Mixin(EntityMaid.class)
public class EntityMaidMixin {

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void pathScript$isInvisibleTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            cir.setReturnValue(player != self.getOwner());
        }
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void pathScript$isPushable(CallbackInfoReturnable<Boolean> cir) {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void pathScript$canBeCollidedWith(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            cir.setReturnValue(false);
        }
    }
}
