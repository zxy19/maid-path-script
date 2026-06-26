package studio.fantasyit.path_script.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import studio.fantasyit.path_script.PathScript;
import studio.fantasyit.path_script.reg.AttachmentRegistry;

@Mixin(EntityMaid.class)
public abstract class EntityMaidMixin extends TamableAnimal {

    protected EntityMaidMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isPushable() {
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"))) {
            return false;
        }
        return super.isPushable();
    }

    @WrapWithCondition(method = "pushEntities", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;pushEntities()V"))
    protected boolean pushEntities(TamableAnimal instance) {
        EntityMaid self = (EntityMaid) (Object) this;
        return !self.getTaskManager().getTask().getUid().equals(PathScript.id("path_navigate"));
    }

    @WrapWithCondition(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/world/backups/MaidBackupsManager;save(Lnet/minecraft/server/MinecraftServer;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)V")
    )
    private boolean onBackupSave(MinecraftServer server, EntityMaid maid) {
        return maid.getData(AttachmentRegistry.PATH_GUIDE_MAID_FOR.get()).isEmpty();
    }
}

