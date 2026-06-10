package me.xjqsh.lrtactical.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.client.renderer.item.MeleeItemRenderer;
import me.xjqsh.lrtactical.init.ModCapabilities;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.CPrepareMeleeAttack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class AttackKeys {
    public static final KeyMapping NORMAL_ATTACK = new KeyMapping("key.lrtactical.normal_attack.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            "key.category.lrtactical");

    public static final KeyMapping SPECIAL_ATTACK = new KeyMapping("key.lrtactical.sp_attack.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "key.category.lrtactical");

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || mc.gameMode == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();

        if (NORMAL_ATTACK.isDown()) {
            var combatProperties = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            if (combatProperties.getCoolDownTick() > 0) return;

            if (combatProperties.preAttack(MeleeAction.LEFT, player.getEyePosition(), player.getLookAngle())) {
                // mc.gameMode.ensureHasSentCarriedItem();
                if (IClientItemExtensions.of(stack).getCustomRenderer() instanceof AnimateGeoItemRenderer renderer) {
                    renderer.triggerAnimation(stack, "attack_left");
                    player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onNormalAttack(InputEvent.MouseButton.Post event) {
        var mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || mc.gameMode == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();

        while (NORMAL_ATTACK.consumeClick()) {
            var combatProperties = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            if (combatProperties.preAttack(MeleeAction.LEFT, player.getEyePosition(), player.getLookAngle())) {
                // mc.gameMode.ensureHasSentCarriedItem();
                if (IClientItemExtensions.of(stack).getCustomRenderer() instanceof AnimateGeoItemRenderer renderer) {
                    renderer.triggerAnimation(stack, "attack_left");
                    player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSpAttack(InputEvent.MouseButton.Post event) {
        var mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || mc.gameMode == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();

        while (SPECIAL_ATTACK.consumeClick()) {
            var combatProperties = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            if (combatProperties.preAttack(MeleeAction.RIGHT, player.getEyePosition(), player.getLookAngle())) {
                // mc.gameMode.ensureHasSentCarriedItem();
                if (IClientItemExtensions.of(stack).getCustomRenderer() instanceof AnimateGeoItemRenderer renderer) {
                    renderer.triggerAnimation(stack, "attack_right");
                    player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSpAttackKey(InputEvent.Key event) {
        var mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || mc.gameMode == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();

        while (SPECIAL_ATTACK.consumeClick()) {
            var combatProperties = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            if (combatProperties.preAttack(MeleeAction.RIGHT, player.getEyePosition(), player.getLookAngle())) {
                // mc.gameMode.ensureHasSentCarriedItem();
                if (IClientItemExtensions.of(stack).getCustomRenderer() instanceof AnimateGeoItemRenderer renderer) {
                    renderer.triggerAnimation(stack, "attack_right");
                    player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }
}
