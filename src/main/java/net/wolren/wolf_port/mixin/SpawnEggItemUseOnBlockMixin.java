package net.wolren.wolf_port.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.wolren.wolf_port.entity.ModEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin({SpawnEggItem.class})
public class SpawnEggItemUseOnBlockMixin extends Item {
    public SpawnEggItemUseOnBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    public void onUse(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = context.getStack();
        if (itemStack.getItem().equals(Items.WOLF_SPAWN_EGG)) {
            World world = context.getWorld();
            PlayerEntity player = context.getPlayer();
            BlockHitResult blockHitResult = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!world.isClient && world instanceof ServerWorld) {
                BlockPos pos;
                if (world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
                    pos = blockPos;
                } else {
                    pos = blockPos.offset(context.getSide());
                }

                EntityType<?> sheep = ModEntities.NEW_WOLF;

                sheep.spawnFromItemStack(
                        (ServerWorld)world,
                        itemStack,
                        context.getPlayer(),
                        pos,
                        SpawnReason.SPAWN_EGG,
                        true,
                        !Objects.equals(blockPos, pos) && context.getSide() == Direction.UP);

                itemStack.decrement(1);
                world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);

                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }


}

