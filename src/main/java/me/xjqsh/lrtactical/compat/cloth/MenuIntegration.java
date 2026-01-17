package me.xjqsh.lrtactical.compat.cloth;


import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.xjqsh.lrtactical.compat.cloth.client.BasicClothConfig;
import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.config.CommonConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.fml.ModLoadingContext;

import javax.annotation.Nullable;

public class MenuIntegration {
    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder root = ConfigBuilder.create().setTitle(Component.literal("LesRaisins Tactical Settings"));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = root.entryBuilder();

        BasicClothConfig.init(root, entryBuilder);
        root.setSavingRunnable(() -> {
            if (ClientConfig.SPEC != null) {
                ClientConfig.SPEC.save();
            }
            if (CommonConfig.SPEC != null) {
                CommonConfig.SPEC.save();
            }
        });

        return root;
    }

    public static void registerModsPage() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (client, parent) -> getConfigScreen(parent));
    }

    public static Screen getConfigScreen(@Nullable Screen parent) {
        return MenuIntegration.getConfigBuilder().setParentScreen(parent).build();
    }
}
