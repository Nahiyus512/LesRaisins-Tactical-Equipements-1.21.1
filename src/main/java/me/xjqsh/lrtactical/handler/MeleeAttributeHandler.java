package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MeleeAttributeHandler {
    private MeleeAttributeHandler() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (!(event.getItemStack().getItem() instanceof IMeleeWeapon)) {
            return;
        }

        LrTacticalAPI.getMeleeIndex(event.getItemStack())
                .ifPresent(index -> index.applyAttributeModifiers(event));
    }
}
