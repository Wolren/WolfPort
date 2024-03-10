package net.wolren.wolf_port;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;
import net.wolren.wolf_port.entity.ModEntities;
import net.wolren.wolf_port.entity.NewWolfEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class WolfPort implements ModInitializer {
    public static final String MOD_ID = "wolf_port";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEntities.registerEntities();
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof WolfEntity && !(entity instanceof NewWolfEntity)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        });

        FabricDefaultAttributeRegistry.register(ModEntities.NEW_WOLF, NewWolfEntity.createWolfAttributes());
    }
}
