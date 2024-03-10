package net.wolren.wolf_port.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.wolren.wolf_port.entity.variant.VariantWolfEntity;
import net.wolren.wolf_port.entity.variant.WolfVariant;
import net.wolren.wolf_port.utils.WolfVariantUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({WolfEntity.class})
public abstract class WolfEntityMixin extends TameableEntity implements Angerable, VariantWolfEntity {
    @Shadow
    public abstract DyeColor getCollarColor();

    protected WolfEntityMixin(EntityType<? extends WolfEntity> entityType, World world) {
        super(entityType, world);
    }

    @Final
    @Shadow
    private static final TrackedData<Boolean> BEGGING = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Final
    @Shadow
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Final
    @Shadow
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique
    private static final TrackedData<Integer> DATA_ID_TYPE_VARIANT = DataTracker.registerData(WolfEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(
            at = {@At("HEAD")},
            method = {"writeCustomDataToNbt"}
    )
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getTypeVariant());
    }

    @Inject(
            at = {@At("HEAD")},
            method = {"readCustomDataFromNbt"}
    )
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, nbt.getInt("Variant"));
        if (this.isTamed()) {
            this.updateAttributesForTamed();
        }
    }

    @Inject(
            at = {@At("HEAD")},
            method = {"createChild(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/PassiveEntity;)Lnet/minecraft/entity/passive/WolfEntity;"},
            cancellable = true)
    public void createChild(ServerWorld serverWorld, PassiveEntity passiveEntity, CallbackInfoReturnable<WolfEntity> cir) {
        WolfEntity wolfEntity = EntityType.WOLF.create(serverWorld);
        if (wolfEntity != null && passiveEntity instanceof WolfEntity wolfEntity2) {
            VariantWolfEntity thisWolf = this;
            VariantWolfEntity wolfEntity2Wolf = (VariantWolfEntity) wolfEntity2;

            WolfVariant newVariant;
            if (this.random.nextBoolean()) {
                newVariant = thisWolf.getVariant();
            } else {
                newVariant = wolfEntity2Wolf.getVariant();
            }

            ((VariantWolfEntity) wolfEntity).setVariant(newVariant);

            if (this.isTamed()) {
                wolfEntity.setOwnerUuid(this.getOwnerUuid());
                wolfEntity.setTamed(true);
                this.updateAttributesForTamed();
                if (this.random.nextBoolean()) {
                    wolfEntity.setCollarColor(this.getCollarColor());
                } else {
                    wolfEntity.setCollarColor(wolfEntity2.getCollarColor());
                }
            }
        }

        cir.setReturnValue(wolfEntity);
    }

    /**
     * @author Wolren
     * @reason To prevent id duplication
     */
    @Overwrite
    public void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BEGGING, false);
        this.dataTracker.startTracking(COLLAR_COLOR, DyeColor.RED.getId());
        this.dataTracker.startTracking(ANGER_TIME, 0);
        this.dataTracker.startTracking(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, @Nullable EntityData entityData,
                                 @Nullable NbtCompound entityNbt) {

        if (spawnReason == SpawnReason.SPAWN_EGG || spawnReason == SpawnReason.MOB_SUMMONED) {
            WolfVariant variant = Util.getRandom(WolfVariant.values(), this.random);
            setVariant(variant);
        } else {
            RegistryKey<Biome> biome = world.getBiome(this.getBlockPos()).getKey().get();
            WolfVariant variant = WolfVariantUtil.fromBiome(biome.getValue());
            setVariant(variant);
        }

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Unique
    private int getTypeVariant() {
        return this.dataTracker.get(DATA_ID_TYPE_VARIANT);
    }

    @Override
    public WolfVariant getVariant() {
        return WolfVariant.byId(this.getTypeVariant() & 255);
    }

    @Unique
    public void setVariant(WolfVariant variant) {
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }

    @Unique
    protected void updateAttributesForTamed() {
        if (this.isTamed()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0F);
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(8.0);
        }
    }
}
