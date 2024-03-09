package net.wolren.wolf_port;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.wolren.wolf_port.entity.ModEntities;
import net.wolren.wolf_port.entity.client.NewWolfRenderer;

@Environment(EnvType.CLIENT)
public class WolfPortClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.NEW_WOLF, NewWolfRenderer::new);
    }
}

