package me.xjqsh.lrtactical.compat.player_animator;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.resource.LrPlayerAnimatorAssetManager;
import me.xjqsh.lrtactical.compat.player_animator.ThirdPersonAnimationConfig.AnimationLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

public class PlayerAnimatorIntegration {
    public static final ResourceLocation UPPER_LAYER = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "upper");
    public static final ResourceLocation LOWER_LAYER = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "lower");
    private static boolean initialized = false;

    private static ResourceLocation getLayerId(AnimationLayer layer) {
        return layer == AnimationLayer.UPPER ? UPPER_LAYER : LOWER_LAYER;
    }

    private static int getLayerPriority(AnimationLayer layer) {
        return layer == AnimationLayer.UPPER ? 40 : 41;
    }


    public static void init() {
        if (initialized) return;
        initialized = true;

        for (AnimationLayer layer : AnimationLayer.values()) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    getLayerId(layer), getLayerPriority(layer), p -> new ModifierLayer<>());
        }

        EquipmentMod.LOGGER.info("Initialized Player Animator third-person animation layers");
    }

    public static KeyframeAnimation getAnimation(ResourceLocation location, String name) {
        return LrPlayerAnimatorAssetManager.INSTANCE.getAnimation(location, name).orElse(null);
    }

    public static void playIdleAnimation(AbstractClientPlayer player, ResourceLocation location, String name, AnimationLayer layer, int fadeInTicks) {
        KeyframeAnimation animation = getAnimation(location, name);
        if (animation == null) return;

        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            IAnimation current = modifierLayer.getAnimation();
            if (current instanceof KeyframeAnimationPlayer kap && kap.isActive()) {
                Object extraData = kap.getData().extraData.get("name");
                if (extraData instanceof String currentName && name.equals(currentName)) {
                    return;
                }
            }
            KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(animation, 0);
            animPlayer.getData().extraData.put("name", name);
            animPlayer.setFirstPersonMode(FirstPersonMode.DISABLED);

            // 抽象的一，但是总之it works
            // Create a frozen snapshot of the current animation state
            modifierLayer.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(fadeInTicks, Ease.INOUTSINE),
                    animPlayer
            );
        }
    }

    public static void playAttackAnimation(AbstractClientPlayer player, ResourceLocation location, String name, AnimationLayer layer, int fadeInTicks) {
        KeyframeAnimation animation = getAnimation(location, name);
        if (animation == null) return;

        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(animation, 0);
            animPlayer.getData().extraData.put("lr_attack_action", true);
            animPlayer.setFirstPersonMode(FirstPersonMode.DISABLED);
            modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeInTicks, Ease.INOUTSINE), animPlayer);
        }
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> getLayer(AbstractClientPlayer player, AnimationLayer layer) {
        return (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(getLayerId(layer));
    }

    public static boolean isAttackAnimationActive(AbstractClientPlayer player, AnimationLayer layer) {
        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            IAnimation current = modifierLayer.getAnimation();
            if (current instanceof KeyframeAnimationPlayer kap) {
                Object extraData = kap.getData().extraData.get("lr_attack_action");
                if (extraData instanceof Boolean isAttack && isAttack) {
                    // Return false when animation reaches endTick, allowing idle animation to start
                    // This prevents the automatic fade-out to static pose
                    return kap.getCurrentTick() < kap.getData().endTick;
                }
            }
        }
        return false;
    }

    public static void stopAnimation(AbstractClientPlayer player, AnimationLayer layer, int fadeOutTicks) {
        ModifierLayer<IAnimation> modifierLayer = getLayer(player, layer);
        if (modifierLayer != null) {
            modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeOutTicks, Ease.INOUTSINE), null);
        }
    }

}
