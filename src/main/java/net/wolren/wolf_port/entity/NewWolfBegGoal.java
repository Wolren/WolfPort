package net.wolren.wolf_port.entity;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class NewWolfBegGoal extends Goal {
    private final NewWolfEntity wolf;
    @Nullable
    private PlayerEntity begFrom;
    private final World world;
    private final float begDistance;
    private int timer;
    private final TargetPredicate validPlayerPredicate;

    public NewWolfBegGoal(NewWolfEntity wolf, float begDistance) {
        this.wolf = wolf;
        this.world = wolf.getWorld();
        this.begDistance = begDistance;
        this.validPlayerPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(begDistance);
        this.setControls(EnumSet.of(Control.LOOK));
    }

    public boolean canStart() {
        this.begFrom = this.world.getClosestPlayer(this.validPlayerPredicate, this.wolf);
        return this.begFrom != null && this.isAttractive(this.begFrom);
    }

    public boolean shouldContinue() {
        if (!this.begFrom.isAlive()) {
            return false;
        } else if (this.wolf.squaredDistanceTo(this.begFrom) > (double)(this.begDistance * this.begDistance)) {
            return false;
        } else {
            return this.timer > 0 && this.isAttractive(this.begFrom);
        }
    }

    public void start() {
        this.wolf.setBegging(true);
        this.timer = this.getTickCount(40 + this.wolf.getRandom().nextInt(40));
    }

    public void stop() {
        this.wolf.setBegging(false);
        this.begFrom = null;
    }

    public void tick() {
        this.wolf.getLookControl().lookAt(this.begFrom.getX(), this.begFrom.getEyeY(), this.begFrom.getZ(), 10.0F, (float)this.wolf.getMaxLookPitchChange());
        --this.timer;
    }

    private boolean isAttractive(PlayerEntity player) {
        for(Hand hand : Hand.values()) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (this.wolf.isTamed() && itemStack.isOf(Items.BONE)) {
                return true;
            }

            if (this.wolf.isBreedingItem(itemStack)) {
                return true;
            }
        }

        return false;
    }
}
