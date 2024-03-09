package net.wolren.wolf_port.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.wolren.wolf_port.WolfPort;

public class ModEntities {
    public static final EntityType<NewWolfEntity> NEW_WOLF = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WolfPort.MOD_ID, "wolf"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NewWolfEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.85f)).build()
    );
    public static void registerEntities() {
        WolfPort.LOGGER.info("Registering Entities for " + WolfPort.MOD_ID);
    }
}
