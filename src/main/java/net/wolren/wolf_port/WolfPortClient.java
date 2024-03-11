package net.wolren.wolf_port;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.EntityType;

@Environment(EnvType.CLIENT)
public class WolfPortClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(EntityType.WOLF, WolfEntityRenderer::new);
    }
}

