package me.xjqsh.lrtactical.compat.player_animator;

import me.xjqsh.lrtactical.client.resource.LrPlayerAnimatorAssetManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

/**
 * Helper class for initializing Player Animator integration
 * Separated to avoid classloading issues when mod is not present
 */
public class PlayerAnimatorHelper {
    private static boolean initialized;

    public static void initIntegration() {
        if (initialized) return;
        initialized = true;

        PlayerAnimatorIntegration.init();
        NeoForge.EVENT_BUS.register(IdleAnimationHandler.class);
        NeoForge.EVENT_BUS.register(MeleeAnimationListener.class);
    }

    public static void registerReloadListener(Consumer<PreparableReloadListener> register) {
        register.accept(LrPlayerAnimatorAssetManager.INSTANCE);
    }
}
