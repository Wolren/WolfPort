package net.wolren.wolf_port.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.wolren.wolf_port.entity.variant.VariantWolfEntity;
import net.wolren.wolf_port.entity.variant.WolfVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Random;

@Mixin(SpawnEggItem.class)
public class WolfBabyMixin extends Item {
    public WolfBabyMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "spawnBaby", cancellable = true)
    public void spawnBaby(PlayerEntity user, MobEntity entity, EntityType<? extends MobEntity> entityType, ServerWorld world, Vec3d pos, ItemStack stack, CallbackInfoReturnable<Optional<MobEntity>> cir) {
        if (entity instanceof WolfEntity) {
            EntityType<? extends MobEntity> babyEntityType = EntityType.WOLF;
            MobEntity babyEntity = babyEntityType.create(world);

            if (babyEntity != null) {
                babyEntity.setBaby(true);
                if (babyEntity.isBaby()) {
                    VariantWolfEntity newWolfBaby = (VariantWolfEntity) babyEntity;
                    WolfVariant randomVariant = Util.getRandom(WolfVariant.values(), new Random());
                    newWolfBaby.setVariant(randomVariant);

                    babyEntity.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                    world.spawnEntityAndPassengers(babyEntity);
                    if (stack.hasCustomName()) {
                        babyEntity.setCustomName(stack.getName());
                    }

                    if (!user.abilities.creativeMode) {
                        stack.decrement(1);
                    }

                    cir.setReturnValue(Optional.of(babyEntity));
                }
            }
        }
    }
}