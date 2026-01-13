package me.xjqsh.lrtactical.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.init.ModEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class BlindnessOverlay {
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (mc.level == null || player == null) {
            return;
        }
        MobEffectInstance effect = player.getEffect(ModEffects.BLIND);
        if (effect == null) {
            return;
        }

        int tickRemain = effect.getDuration();

        int alpha = tickRemain > 100 ? 255 : (int) (tickRemain / 100f * 255f);
        int color = (ClientConfig.BLACK_FLASH.get() ? 0x000000 : 0xFFFFFF) + (alpha << 24);

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        graphics.fill(0, 0, width, height, color);

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
