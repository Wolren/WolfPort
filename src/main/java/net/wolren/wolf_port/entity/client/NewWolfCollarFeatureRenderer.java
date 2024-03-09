package net.wolren.wolf_port.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.wolren.wolf_port.entity.NewWolfEntity;

@Environment(EnvType.CLIENT)
public class NewWolfCollarFeatureRenderer extends FeatureRenderer<NewWolfEntity, NewWolfEntityModel<NewWolfEntity>> {
    private static final Identifier SKIN = new Identifier("textures/entity/wolf/wolf_collar.png");

    public NewWolfCollarFeatureRenderer(FeatureRendererContext<NewWolfEntity, NewWolfEntityModel<NewWolfEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int i,
            NewWolfEntity wolfEntity,
            float f,
            float g,
            float h,
            float j,
            float k,
            float l
    ) {
        if (wolfEntity.isTamed() && !wolfEntity.isInvisible()) {
            float[] fs = wolfEntity.getCollarColor().getColorComponents();
            renderModel(this.getContextModel(), SKIN, matrixStack, vertexConsumerProvider, i, wolfEntity, fs[0], fs[1], fs[2]);
        }
    }
}

