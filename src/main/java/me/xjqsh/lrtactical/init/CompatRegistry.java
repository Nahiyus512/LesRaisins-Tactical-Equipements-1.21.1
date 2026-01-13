package me.xjqsh.lrtactical.init;

import me.xjqsh.lrtactical.client.gui.ClothConfigScreen;
import me.xjqsh.lrtactical.compat.cloth.MenuIntegration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class CompatRegistry {
    public static final String CLOTH_CONFIG = "cloth_config";


    @SubscribeEvent
    public static void onEnqueue(final InterModEnqueueEvent event) {
        event.enqueueWork(() -> checkModLoad(CLOTH_CONFIG, () -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                MenuIntegration.registerModsPage();
            }
        }));
        event.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClothConfigScreen.registerNoClothConfigPage();
            }
        });
    }

    public static void checkModLoad(String modId, Runnable runnable) {
        if (ModList.get().isLoaded(modId)) {
            runnable.run();
        }
    }
}
