package net.wolren.wolf_port.entity.variant;

import java.util.Arrays;
import java.util.Comparator;

public enum WolfVariant {
    PALE(0),
    SPOTTED(1),
    SNOWY(2),
    BLACK(3),
    ASHEN(4),
    RUSTY(5),
    WOODS(6),
    CHESTNUT(7),
    STRIPED(8);


    private static final WolfVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(WolfVariant::getId)).toArray(WolfVariant[]::new);
    private final int id;

    WolfVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static WolfVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }
}
