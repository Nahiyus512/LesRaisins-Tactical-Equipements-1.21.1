package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModCapabilities {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EquipmentMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CustomItemCoolDowns>> CUSTOM_COOLDOWN = ATTACHMENT_TYPES.register(
            "custom_cooldown",
            () -> AttachmentType.builder(holder -> new CustomItemCoolDowns((net.minecraft.world.entity.player.Player) holder)).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CombatProperties>> COMBAT_PROPERTIES = ATTACHMENT_TYPES.register(
            "combat_properties",
            () -> AttachmentType.builder(holder -> new CombatProperties((net.minecraft.world.entity.player.Player) holder)).build()
    );
}