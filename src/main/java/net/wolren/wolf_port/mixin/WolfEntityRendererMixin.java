package net.wolren.wolf_port.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;
import net.wolren.wolf_port.WolfPort;
import net.wolren.wolf_port.entity.variant.VariantWolfEntity;
import net.wolren.wolf_port.entity.variant.WolfVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WolfEntityRenderer.class)
public abstract class WolfEntityRendererMixin extends MobEntityRenderer<WolfEntity, WolfEntityModel<WolfEntity>>  {
    public WolfEntityRendererMixin(EntityRendererFactory.Context context, WolfEntityModel<WolfEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "getTexture(Lnet/minecraft/entity/passive/WolfEntity;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true)
    private void getTexture(WolfEntity entity, CallbackInfoReturnable<Identifier> cir) {
        VariantWolfEntity variantEntity = (VariantWolfEntity) entity;
        WolfVariant variant = variantEntity.getVariant();
        String basePath = "textures/entity/wolf/" + variant.name().toLowerCase();

        if (entity.isTamed()) {
            basePath += "_tamed";
        }
        if (entity.hasAngerTime() && !entity.isTamed()) {
            basePath += "_angry";
        }

        basePath += ".png";

        cir.setReturnValue(new Identifier(WolfPort.MOD_ID, basePath));
    }

    @Inject(method = "render(Lnet/minecraft/entity/passive/WolfEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    public void render(WolfEntity wolfEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (wolfEntity.isFurWet()) {
            float h = wolfEntity.getFurWetBrightnessMultiplier(g);
            this.model.setColorMultiplier(h, h, h);
        }

        super.render(wolfEntity, f, g, matrixStack, vertexConsumerProvider, i);
        if (wolfEntity.isFurWet()) {
            this.model.setColorMultiplier(1.0F, 1.0F, 1.0F);
        }
    }
}