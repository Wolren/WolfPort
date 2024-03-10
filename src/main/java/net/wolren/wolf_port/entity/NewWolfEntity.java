package net.wolren.wolf_port.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.EntityView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.wolren.wolf_port.entity.variant.WolfVariant;
import net.wolren.wolf_port.utils.WolfVariantUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

public class NewWolfEntity extends WolfEntity implements Angerable {
    private static final TrackedData<Boolean> BEGGING = DataTracker.registerData(NewWolfEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> COLLAR_COLOR = DataTracker.registerData(NewWolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(NewWolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final Predicate<LivingEntity> FOLLOW_TAMED_PREDICATE = entity -> {
        EntityType<?> entityType = entity.getType();
        return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
    };
    private static final float WILD_MAX_HEALTH = 8.0F;
    private static final float TAMED_MAX_HEALTH = 40.0F;
    private static final float field_49237 = 0.125F;
    private float begAnimationProgress;
    private float lastBegAnimationProgress;
    private boolean furWet;
    private boolean canShakeWaterOff;
    private float shakeProgress;
    private float lastShakeProgress;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    @Nullable
    private UUID angryAt;

    public NewWolfEntity(EntityType<? extends NewWolfEntity> entityType, World world) {
        super(entityType, world);
        this.setTamed(false);
        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0F);
    }

    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new NewWolfEntity.WolfEscapeDangerGoal(1.5));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(3, new NewWolfEntity.AvoidLlamaGoal<>(this, LlamaEntity.class, 24.0F, 1.5, 1.5));
        this.goalSelector.add(4, new PounceAtTargetGoal(this, 0.4F));
        this.goalSelector.add(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));
        this.goalSelector.add(7, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(9, new NewWolfBegGoal(this, 8.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(5, new UntamedActiveTargetGoal<>(this, AnimalEntity.class, false, FOLLOW_TAMED_PREDICATE));
        this.targetSelector.add(6, new UntamedActiveTargetGoal<>(this, TurtleEntity.class, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
        this.targetSelector.add(7, new ActiveTargetGoal<>(this, AbstractSkeletonEntity.class, false));
        this.targetSelector.add(8, new UniversalAngerGoal<>(this, true));
    }

    public static DefaultAttributeContainer.Builder createWolfAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3F)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
    }


    protected SoundEvent getAmbientSound() {
        if (this.hasAngerTime()) {
            return SoundEvents.ENTITY_WOLF_GROWL;
        } else if (this.random.nextInt(3) == 0) {
            return this.isTamed() && this.getHealth() < 20.0F ? SoundEvents.ENTITY_WOLF_WHINE : SoundEvents.ENTITY_WOLF_PANT;
        } else {
            return SoundEvents.ENTITY_WOLF_AMBIENT;
        }
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WOLF_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WOLF_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient && this.furWet && !this.canShakeWaterOff && !this.isNavigating() && this.isOnGround()) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0F;
            this.lastShakeProgress = 0.0F;
            this.getWorld().sendEntityStatus(this, (byte) 8);
        }

        if (!this.getWorld().isClient) {
            this.tickAngerLogic((ServerWorld) this.getWorld(), true);
        }
    }

    public void tick() {
        super.tick();
        if (this.isAlive()) {
            this.lastBegAnimationProgress = this.begAnimationProgress;
            if (this.isBegging()) {
                this.begAnimationProgress += (1.0F - this.begAnimationProgress) * 0.4F;
            } else {
                this.begAnimationProgress += (0.0F - this.begAnimationProgress) * 0.4F;
            }

            if (this.isWet()) {
                this.furWet = true;
                if (this.canShakeWaterOff && !this.getWorld().isClient) {
                    this.getWorld().sendEntityStatus(this, (byte) 56);
                    this.resetShake();
                }
            } else if ((this.furWet || this.canShakeWaterOff) && this.canShakeWaterOff) {
                if (this.shakeProgress == 0.0F) {
                    this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    this.emitGameEvent(GameEvent.ENTITY_ACTION);
                }

                this.lastShakeProgress = this.shakeProgress;
                this.shakeProgress += 0.05F;
                if (this.lastShakeProgress >= 2.0F) {
                    this.furWet = false;
                    this.canShakeWaterOff = false;
                    this.lastShakeProgress = 0.0F;
                    this.shakeProgress = 0.0F;
                }

                if (this.shakeProgress > 0.4F) {
                    float f = (float) this.getY();
                    int i = (int) (MathHelper.sin((this.shakeProgress - 0.4F) * (float) Math.PI) * 7.0F);
                    Vec3d vec3d = this.getVelocity();

                    for (int j = 0; j < i; ++j) {
                        float g = (this.random.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                        float h = (this.random.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                        this.getWorld()
                                .addParticle(ParticleTypes.SPLASH, this.getX() + (double) g, f + 0.8F, this.getZ() + (double) h, vec3d.x, vec3d.y, vec3d.z);
                    }
                }
            }
        }
    }

    private void resetShake() {
        this.canShakeWaterOff = false;
        this.shakeProgress = 0.0F;
        this.lastShakeProgress = 0.0F;
    }

    public void onDeath(DamageSource damageSource) {
        this.furWet = false;
        this.canShakeWaterOff = false;
        this.lastShakeProgress = 0.0F;
        this.shakeProgress = 0.0F;
        super.onDeath(damageSource);
    }

    public boolean isFurWet() {
        return this.furWet;
    }

    public float getFurWetBrightnessMultiplier(float tickDelta) {
        return Math.min(0.5F + MathHelper.lerp(tickDelta, this.lastShakeProgress, this.shakeProgress) / 2.0F * 0.5F, 1.0F);
    }

    public float getShakeAnimationProgress(float tickDelta, float f) {
        float g = (MathHelper.lerp(tickDelta, this.lastShakeProgress, this.shakeProgress) + f) / 1.8F;
        if (g < 0.0F) {
            g = 0.0F;
        } else if (g > 1.0F) {
            g = 1.0F;
        }

        return MathHelper.sin(g * (float) Math.PI) * MathHelper.sin(g * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
    }

    public float getBegAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastBegAnimationProgress, this.begAnimationProgress) * 0.15F * (float) Math.PI;
    }

    public int getMaxLookPitchChange() {
        return this.isInSittingPose() ? 20 : super.getMaxLookPitchChange();
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.getWorld().isClient) {
                this.setSitting(false);
            }

            return super.damage(source, amount);
        }
    }

    public boolean tryAttack(Entity target) {
        boolean bl = target.damage(this.getDamageSources().mobAttack(this), (float) ((int) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
        if (bl) {
            this.applyDamageEffects(this, target);
        }

        return bl;
    }

    protected void updateAttributesForTamed() {
        if (this.isTamed()) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0F);
        } else {
            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(8.0);
        }
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (this.getWorld().isClient) {
            boolean bl = this.isOwner(player) || this.isTamed() || itemStack.isOf(Items.BONE) && !this.isTamed() && !this.hasAngerTime();
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        } else if (this.isTamed()) {
            if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                this.heal((float)item.getFoodComponent().getHunger());
                return ActionResult.SUCCESS;
            } else {
                if (item instanceof DyeItem dyeItem && this.isOwner(player)) {
                    DyeColor dyeColor = dyeItem.getColor();
                    if (dyeColor != this.getCollarColor()) {
                        this.setCollarColor(dyeColor);
                        if (!player.getAbilities().creativeMode) {
                            itemStack.decrement(1);
                        }

                        return ActionResult.SUCCESS;
                    }

                    return super.interactMob(player, hand);
                }

                ActionResult actionResult = super.interactMob(player, hand);
                if ((!actionResult.isAccepted() || this.isBaby()) && this.isOwner(player)) {
                    this.setSitting(!this.isSitting());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                    return ActionResult.SUCCESS;
                } else {
                    return actionResult;
                }
            }
        } else if (itemStack.isOf(Items.BONE) && !this.hasAngerTime()) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            if (this.random.nextInt(3) == 0) {
                this.setOwner(player);
                this.updateAttributesForTamed();
                this.navigation.stop();
                this.setTarget(null);
                this.setSitting(true);
                this.getWorld().sendEntityStatus(this, (byte)7);
            } else {
                this.getWorld().sendEntityStatus(this, (byte)6);
            }

            return ActionResult.SUCCESS;
        } else {
            return super.interactMob(player, hand);
        }
    }

    public void handleStatus(byte status) {
        if (status == 8) {
            this.canShakeWaterOff = true;
            this.shakeProgress = 0.0F;
            this.lastShakeProgress = 0.0F;
        } else if (status == 56) {
            this.resetShake();
        } else {
            super.handleStatus(status);
        }
    }

    public float getTailAngle() {
        if (this.hasAngerTime()) {
            return 1.5393804F;
        } else if (this.isTamed()) {
            float f = this.getMaxHealth();
            float g = (f - this.getHealth()) / f;
            return (0.55F - g * 0.4F) * (float) Math.PI;
        } else {
            return (float) (Math.PI / 5);
        }
    }

    public boolean isBreedingItem(ItemStack stack) {
        Item item = stack.getItem();
        return item.isFood() && item.getFoodComponent().isMeat();
    }

    public int getLimitPerChunk() {
        return 8;
    }

    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TIME);
    }

    public void setAngerTime(int angerTime) {
        this.dataTracker.set(ANGER_TIME, angerTime);
    }

    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.dataTracker.get(COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor color) {
        this.dataTracker.set(COLLAR_COLOR, color.getId());
    }

    @Nullable
    public NewWolfEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        NewWolfEntity wolfEntity = ModEntities.NEW_WOLF.create(serverWorld);
        if (wolfEntity != null && passiveEntity instanceof NewWolfEntity wolfEntity2) {
            if (this.random.nextBoolean()) {
                wolfEntity.setVariant(this.getVariant());
            } else {
                wolfEntity.setVariant(wolfEntity2.getVariant());
            }

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

        return wolfEntity;
    }

    public void setBegging(boolean begging) {
        this.dataTracker.set(BEGGING, begging);
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        } else if (!this.isTamed()) {
            return false;
        } else if (!(other instanceof NewWolfEntity)) {
            return false;
        } else {
            NewWolfEntity wolfEntity = (NewWolfEntity) other;
            if (!wolfEntity.isTamed()) {
                return false;
            } else if (wolfEntity.isInSittingPose()) {
                return false;
            } else {
                return this.isInLove() && wolfEntity.isInLove();
            }
        }
    }

    public boolean isBegging() {
        return this.dataTracker.get(BEGGING);
    }

    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof CreeperEntity || target instanceof GhastEntity) {
            return false;
        } else if (target instanceof WolfEntity wolfEntity) {
            return !wolfEntity.isTamed() || wolfEntity.getOwner() != owner;
        } else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).shouldDamagePlayer((PlayerEntity)target)) {
            return false;
        } else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity)target).isTame()) {
            return false;
        } else {
            return !(target instanceof TameableEntity) || !((TameableEntity)target).isTamed();
        }
    }

    public boolean canBeLeashedBy(PlayerEntity player) {
        return !this.hasAngerTime() && super.canBeLeashedBy(player);
    }

    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6F * this.getStandingEyeHeight(), this.getWidth() * 0.4F);
    }

    @Override
    public EntityView method_48926() {
        return getEntityWorld();
    }

    class AvoidLlamaGoal<T extends LivingEntity> extends FleeEntityGoal<T> {
        private final NewWolfEntity wolf;

        public AvoidLlamaGoal(NewWolfEntity wolf, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(wolf, fleeFromType, distance, slowSpeed, fastSpeed);
            this.wolf = wolf;
        }

        public boolean canStart() {
            if (super.canStart() && this.targetEntity instanceof LlamaEntity) {
                return !this.wolf.isTamed() && this.isScaredOf((LlamaEntity)this.targetEntity);
            } else {
                return false;
            }
        }

        private boolean isScaredOf(LlamaEntity llama) {
            return llama.getStrength() >= NewWolfEntity.this.random.nextInt(5);
        }

        public void start() {
            NewWolfEntity.this.setTarget(null);
            super.start();
        }

        public void tick() {
            NewWolfEntity.this.setTarget(null);
            super.tick();
        }
    }

    class WolfEscapeDangerGoal extends EscapeDangerGoal {
        public WolfEscapeDangerGoal(double speed) {
            super(NewWolfEntity.this, speed);
        }

        protected boolean isInDanger() {
            return this.mob.shouldEscapePowderSnow() || this.mob.isOnFire();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("CollarColor", (byte) this.getCollarColor().getId());
        nbt.putInt("Variant", this.getTypeVariant());
        this.writeAngerToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, nbt.getInt("Variant"));
        if (nbt.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(nbt.getInt("CollarColor")));
        }
        if (this.isTamed()) {
            this.updateAttributesForTamed();
        }

        this.readAngerFromNbt(this.getWorld(), nbt);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_ID_TYPE_VARIANT, 0);
        this.dataTracker.startTracking(BEGGING, false);
        this.dataTracker.startTracking(COLLAR_COLOR, DyeColor.RED.getId());
        this.dataTracker.startTracking(ANGER_TIME, 0);
    }

    private static final TrackedData<Integer> DATA_ID_TYPE_VARIANT =
            DataTracker.registerData(NewWolfEntity.class, TrackedDataHandlerRegistry.INTEGER);

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

    public WolfVariant getVariant() {
        return WolfVariant.byId(this.getTypeVariant() & 255);
    }

    private int getTypeVariant() {
        return this.dataTracker.get(DATA_ID_TYPE_VARIANT);
    }

    public void setVariant(WolfVariant variant) {
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }
}
