package net.wolren.wolf_port.mixin;


import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.world.ServerWorld;
import net.wolren.wolf_port.entity.NewWolfEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ServerWorld.class})
public class SpawnHelperMixin {
    public SpawnHelperMixin() {
    }

    @Inject(
            at = {@At("HEAD")},
            method = {"addEntity"},
            cancellable = true
    )
    private void blacklist(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity instanceof WolfEntity && !(entity instanceof NewWolfEntity)) {
            entity.setRemoved(Entity.RemovalReason.DISCARDED);
            info.setReturnValue(false);
        }
    }
}

