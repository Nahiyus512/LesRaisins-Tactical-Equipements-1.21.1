package me.xjqsh.lrtactical.capability;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class TickHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        player.getData(me.xjqsh.lrtactical.init.ModCapabilities.CUSTOM_COOLDOWN).tick();
        player.getData(me.xjqsh.lrtactical.init.ModCapabilities.COMBAT_PROPERTIES).tick();
    }
}
