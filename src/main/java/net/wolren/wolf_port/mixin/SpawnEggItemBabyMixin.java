package net.wolren.wolf_port.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.wolren.wolf_port.WolfPort;
import net.wolren.wolf_port.entity.ModEntities;
import net.wolren.wolf_port.entity.NewWolfEntity;
import net.wolren.wolf_port.entity.variant.WolfVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin({SpawnEggItem.class})
public class SpawnEggItemBabyMixin extends Item {
    public SpawnEggItemBabyMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "spawnBaby", cancellable = true)
    public void spawnBaby(PlayerEntity user, MobEntity entity, EntityType<? extends MobEntity> entityType, ServerWorld world, Vec3d pos, ItemStack stack, CallbackInfoReturnable<Optional<MobEntity>> cir) {
        if (entity instanceof NewWolfEntity) {
            EntityType<? extends MobEntity> babyEntityType = ModEntities.NEW_WOLF;
            MobEntity babyEntity = babyEntityType.create(world);

            if (babyEntity != null) {
                babyEntity.setBaby(true);
                if (babyEntity.isBaby()) {
                    NewWolfEntity newWolfBaby = (NewWolfEntity) babyEntity;
                    WolfVariant randomVariant = Util.getRandom(WolfVariant.values(), newWolfBaby.getRandom());
                    newWolfBaby.setVariant(randomVariant);


                    babyEntity.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                    world.spawnEntityAndPassengers(babyEntity);
                    if (stack.hasCustomName()) {
                        babyEntity.setCustomName(stack.getName());
                    }

                    if (!user.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }

                    cir.setReturnValue(Optional.of(babyEntity));
                }
            }
        }
    }
}

