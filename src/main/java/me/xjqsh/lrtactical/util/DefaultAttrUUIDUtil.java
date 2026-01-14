package me.xjqsh.lrtactical.util;

import com.google.common.collect.Maps;
import me.xjqsh.lrtactical.mixin.ItemAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.UUID;

public class DefaultAttrUUIDUtil {
    private static final Map<ResourceLocation, UUID> cache = Maps.newHashMap();
    // 这俩有特殊作用
    static {
        cache.put(ResourceLocation.parse("minecraft:generic.attack_damage"), ItemAccessor.lrtactical_getBaseAttackDamageUuid());
        cache.put(ResourceLocation.parse("minecraft:generic.attack_speed"), ItemAccessor.lrtactical_getBaseAttackSpeedUuid());
    }

    public static UUID getUUID(ResourceLocation id) {
        return cache.computeIfAbsent(id, k -> UUID.randomUUID());
    }
}
