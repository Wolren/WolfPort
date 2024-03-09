package net.wolren.wolf_port.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldView;
import net.wolren.wolf_port.entity.ModEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SpawnHelper.class})
public class CanSpawnMixin {
    @Inject(at = @At("HEAD"), method = "canSpawn(Lnet/minecraft/entity/SpawnRestriction$Location;Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityType;)Z", cancellable = true)
    private static void canSpawnWolf(
            SpawnRestriction.Location location, WorldView world, BlockPos pos, EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        if (entityType == ModEntities.NEW_WOLF) {
           cir.setReturnValue(world.getBlockState(pos.down()).isIn(BlockTags.WOLVES_SPAWNABLE_ON));
        }
    }
}
