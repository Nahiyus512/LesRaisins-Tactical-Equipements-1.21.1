package me.xjqsh.lrtactical.util;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.UUID;

public class DefaultAttrUUIDUtil {
    private static final Map<ResourceLocation, UUID> cache = Maps.newHashMap();
    // 这俩有特殊作用
    static {
        cache.put(ResourceLocation.parse("minecraft:generic.attack_damage"), UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"));
        cache.put(ResourceLocation.parse("minecraft:generic.attack_speed"), UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"));
    }

    public static UUID getUUID(ResourceLocation id) {
        return cache.computeIfAbsent(id, k -> UUID.randomUUID());
    }
}
