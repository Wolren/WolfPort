package net.wolren.wolf_port.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.wolren.wolf_port.WolfPort;
import net.wolren.wolf_port.entity.NewWolfEntity;

@Environment(EnvType.CLIENT)
public class NewWolfRenderer extends MobEntityRenderer<NewWolfEntity, NewWolfEntityModel<NewWolfEntity>> {
    public static String path = "textures/entity/wolf/";

    public NewWolfRenderer(EntityRendererFactory.Context context) {
        super(context, new NewWolfEntityModel<>(context.getPart(EntityModelLayers.WOLF)), 0.5F);
        this.addFeature(new NewWolfCollarFeatureRenderer(this));
    }

    protected float getAnimationProgress(NewWolfEntity wolfEntity, float f) {
        return wolfEntity.getTailAngle();
    }

    public void render(NewWolfEntity wolfEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        if (wolfEntity.isFurWet()) {
            float h = wolfEntity.getFurWetBrightnessMultiplier(g);
            this.model.setColorMultiplier(h, h, h);
        }

        super.render(wolfEntity, f, g, matrixStack, vertexConsumerProvider, i);
        if (wolfEntity.isFurWet()) {
            this.model.setColorMultiplier(1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public Identifier getTexture(NewWolfEntity entity) {
        String basePath = path + entity.getVariant().name().toLowerCase();

        if (entity.isTamed()) {
            basePath += "_tamed";
        }
        if (entity.hasAngerTime()) {
            basePath += "_angry";
        }

        basePath += ".png";

        return new Identifier(WolfPort.MOD_ID, basePath);
    }
}
