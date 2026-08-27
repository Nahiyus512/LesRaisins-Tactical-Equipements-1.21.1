package me.xjqsh.lrtactical.compat.player_animator;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.resource.LrPlayerAnimatorAssetManager;
import me.xjqsh.lrtactical.compat.player_animator.ThirdPersonAnimationConfig.AnimationLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PlayerAnimatorIntegration {
    public static final ResourceLocation UPPER_LAYER = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "upper");
    public static final ResourceLocation LOWER_LAYER = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "lower");
    public static final ResourceLocation ROTATION_LAYER = ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "rotation");
    private static boolean initialized = false;

    private static ResourceLocation getLayerId(AnimationLayer layer) {
        return switch (layer) {
            case UPPER -> UPPER_LAYER;
            case LOWER -> LOWER_LAYER;
            case ROTATION -> ROTATION_LAYER;
        };
    }

    private static int getLayerPriority(AnimationLayer layer) {
        return switch (layer) {
            case UPPER -> 40;
            case LOWER -> 41;
            case ROTATION -> 42;
        };
    }


    public static void init() {
        if (initialized) return;
        initialized = true;

        for (AnimationLayer layer : AnimationLayer.values()) {
            if (layer == AnimationLayer.ROTATION) {
                continue;
            }
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    getLayerId(layer), getLayerPriority(layer), p -> new ModifierLayer<>());
        }

        // 转身修正层：通过 AdjustmentModifier 实时修正身体/头/手臂朝向
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ROTATION_LAYER, 42, player -> new ModifierLayer<>(null, AdjustmentYRotModifier.getModifier(player)));

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

            // Create a frozen snapshot of the current animation state,
            // preventing the animation from advancing during fade transitions
            IAnimation frozenCurrent = current instanceof KeyframeAnimationPlayer kap ?
                    new FrozenAnimationSnapshot(kap) : current;

            // Use custom fade modifier that always reads from beginAnimation, even if inactive
            var fadeModifier = new ForcedFadeModifier(fadeInTicks, Ease.INOUTSINE);
            fadeModifier.setBeginAnimation(frozenCurrent);
            modifierLayer.addModifierLast(fadeModifier);
            modifierLayer.setAnimation(animPlayer);
        }
    }

    /**
     * Frozen snapshot of an animation at a specific tick.
     * This prevents the animation from advancing during fade transitions.
     */
    private static class FrozenAnimationSnapshot implements IAnimation {
        private final KeyframeAnimationPlayer source;

        public FrozenAnimationSnapshot(KeyframeAnimationPlayer source) {
            this.source = source;
        }

        @Override
        public void tick() {
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
            return source.get3DTransform(modelName, type, 0, value0);
        }

        @Override
        public void setupAnim(float tickDelta) {
            source.setupAnim(0);
        }
    }

    private static class ForcedFadeModifier extends AbstractFadeModifier {
        private final Ease ease;

        protected ForcedFadeModifier(int length, Ease ease) {
            super(length);
            this.ease = ease;
        }

        @Override
        protected float getAlpha(String modelName, TransformType type, float progress) {
            return ease.invoke(progress);
        }

        @Override
        public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
            if (calculateProgress(tickDelta) > 1) {
                return super.get3DTransform(modelName, type, tickDelta, value0);
            }

            Vec3f animatedVec = super.get3DTransform(modelName, type, tickDelta, value0);
            float a = getAlpha(modelName, type, calculateProgress(tickDelta));

            Vec3f source = beginAnimation != null ?
                    beginAnimation.get3DTransform(modelName, type, tickDelta, value0) : value0;

            return animatedVec.scale(a).add(source.scale(1 - a));
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

    @SuppressWarnings("unchecked")
    public static void enableRotationModifier(AbstractClientPlayer player, ResourceLocation location, int fadeInTicks) {
        ModifierLayer<IAnimation> rotationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(ROTATION_LAYER);
        if (rotationLayer != null && rotationLayer.getAnimation() == null) {
            rotationLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeInTicks, Ease.INOUTSINE), new EmptyActiveAnimation());
        }
    }

}
