package com.kesselot.dragonmeal;

import net.minecraft.world.item.Item;

public class DragonFertilityCharmItem extends Item {

    public enum CharmType {
        PARTIAL(4000), // 200 seconds = 4000 ticks
        FULL(0);

        public final int reductionTicks;
        CharmType(int reductionTicks) {
            this.reductionTicks = reductionTicks;
        }
    }

    public final CharmType type;

    public DragonFertilityCharmItem(CharmType type, Properties properties) {
        super(properties);
        this.type = type;
    }
}