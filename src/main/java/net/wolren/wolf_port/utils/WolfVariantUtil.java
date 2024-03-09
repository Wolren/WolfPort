package net.wolren.wolf_port.utils;

import net.minecraft.util.Identifier;
import net.wolren.wolf_port.entity.variant.WolfVariant;

import java.util.HashMap;
import java.util.Map;

public class WolfVariantUtil {
    private static final Map<Identifier, WolfVariant> BIOME_TO_VARIANT_MAP = new HashMap<>();

    static {
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:taiga"), WolfVariant.PALE);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:forest"), WolfVariant.WOODS);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:snowy_taiga"), WolfVariant.ASHEN);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:old_growth_pine_taiga"), WolfVariant.BLACK);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:old_growth_spruce_taiga"), WolfVariant.CHESTNUT);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:sparse_jungle"), WolfVariant.RUSTY);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:savanna_plateau"), WolfVariant.SPOTTED);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:wooded_badlands"), WolfVariant.STRIPED);
        BIOME_TO_VARIANT_MAP.put(new Identifier("minecraft:grove"), WolfVariant.SNOWY);
    }

    public static WolfVariant fromBiome(Identifier biomeId) {
        WolfVariant variant = BIOME_TO_VARIANT_MAP.get(biomeId);
        if (variant == null) {
            variant = WolfVariant.PALE;
        }
        return variant;
    }
}

