package me.xjqsh.lrtactical.item;

import com.tacz.guns.api.item.IAnimationItem;
import me.xjqsh.lrtactical.api.event.ConsumableUseEvent;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import me.xjqsh.lrtactical.client.input.ConsumableInputHandler;
import me.xjqsh.lrtactical.client.renderer.item.ConsumableItemRenderer;
import me.xjqsh.lrtactical.init.ModCapabilities;
import me.xjqsh.lrtactical.inventory.tooltip.ConsumableTooltip;
import me.xjqsh.lrtactical.item.consumable.ConsumableData;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ConsumableItem extends Item implements IAnimationItem, IConsumable {
    public ConsumableItem() {
        super(new Properties().stacksTo(1).durability(1));
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return this.getConsumableIndex(stack).map(ConsumableIndex::getFoodProperties).orElse(null);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return this.getConsumableIndex(stack).map(ConsumableIndex::getMaxStackSize).orElse(1);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return this.getConsumableIndex(stack)
                .map(index -> index.getData().getMaxDurability())
                .orElse(0);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return this.getMaxDamage(stack) > 0;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return this.getConsumableIndex(stack).map(index -> {
            if (index.getData().isToggleUse()) {
                return 72000;
            }
            return index.getData().getUseDuration();
        }).orElse(0);
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @NotNull
    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.EMPTY;
    }

    @Override
    public int getDrawTime(ItemStack stack) {
        return this.getConsumableIndex(stack).map(index -> index.getData().getDrawTime()).orElse(0);
    }

    @Override
    public int getPutAwayTime(ItemStack stack) {
        return this.getConsumableIndex(stack).map(index -> index.getData().getPutAwayTime()).orElse(0);
    }

    @SuppressWarnings("removal")
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ConsumableItemRenderer renderer = null;

            @Override
            public ConsumableItemRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ConsumableItemRenderer();
                }
                return renderer;
            }
        });
    }

    @NotNull
    @Override
    public String getDescriptionId(@NotNull ItemStack stack) {
        return this.getConsumableIndex(stack).map(ConsumableIndex::getDescriptionId).orElse(super.getDescriptionId(stack));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        if (usedHand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }

        if (player.isUsingItem()) {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }

        ItemStack stack = player.getItemInHand(usedHand);
        var combatProperties = player.getData(ModCapabilities.COMBAT_PROPERTIES);
        if (combatProperties.getCoolDownTick() > 0) {
            return InteractionResultHolder.fail(stack);
        }

        boolean onCooldown = getCoolDownId(stack)
                .map(id -> {
                    CustomItemCoolDowns cooldowns = player.getData(ModCapabilities.CUSTOM_COOLDOWN);
                    return cooldowns.isOnCooldown(id);
                }).orElse(false);
        if (onCooldown) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(usedHand);

        // 客户端：标记TOGGLE使用状态
        if (level.isClientSide()) {
            this.getConsumableIndex(stack).ifPresent(index -> {
                if (index.getData().isToggleUse()) {
                    ConsumableInputHandler.startToggleUsing(usedHand);
                }
            });
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return finishConsumableUse(stack, level, entity);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        this.getConsumableIndex(stack).ifPresent(index -> {
            if (index.getData().isToggleUse()) {
                if (!level.isClientSide() && entity instanceof Player player) {
                    var combatProps = player.getData(ModCapabilities.COMBAT_PROPERTIES);
                    combatProps.setToggleUseTicks(combatProps.getToggleUseTicks() + 1);
                    if (combatProps.getToggleUseTicks() >= index.getData().getUseDuration()) {
                        finishConsumableUse(stack, level, entity);
                        entity.stopUsingItem();
                        combatProps.setToggleUseTicks(0);
                    }
                }
            }
        });
    }

    private ItemStack finishConsumableUse(ItemStack stack, Level level, LivingEntity entity) {
        this.getConsumableIndex(stack).ifPresent(index -> {
            if (!level.isClientSide()) {
                applyEffects(entity, stack, index);
                if (entity instanceof Player player) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (!player.getAbilities().instabuild) {
                        consumeAfterUse(stack, entity, index);
                    }
                } else {
                    consumeAfterUse(stack, entity, index);
                }
            }
        });
        return stack;
    }

    private void consumeAfterUse(ItemStack stack, LivingEntity entity, ConsumableIndex index) {
        ConsumableData data = index.getData();
        if (data.hasDurability()) {
            int newDamage = stack.getDamageValue() + data.getDurabilityDamage();
            if (newDamage >= data.getMaxDurability()) {
                stack.shrink(1);
            } else {
                stack.setDamageValue(newDamage);
            }
        } else {
            stack.shrink(1);
        }
    }

    private void removeEffect(LivingEntity entity, ConsumableData.RemoveEffectSelector selector) {
        if (selector.isCategory()) {
            removeEffectsByCategory(entity, selector.getCategory());
            return;
        }

        Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(selector.getEffect()).orElse(null);
        if (effect != null) {
            entity.removeEffect(effect);
        }
    }

    private void removeEffectsByCategory(LivingEntity entity, MobEffectCategory category) {
        List<Holder<MobEffect>> effects = entity.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == category)
                .toList();

        for (Holder<MobEffect> effect : effects) {
            entity.removeEffect(effect);
        }
    }

    private void applyEffects(LivingEntity entity, ItemStack stack, ConsumableIndex index) {
        ConsumableData data = index.getData();
        if (data.getHeal() > 0f) {
            entity.heal(data.getHeal());
        }

        for (ConsumableData.RemoveEffectSelector selector : data.getRemoveEffects()) {
            removeEffect(entity, selector);
        }

        for (ConsumableData.EffectData effectData : data.getEffects()) {
            if (entity.getRandom().nextFloat() > effectData.getChance()) {
                continue;
            }
            MobEffectInstance effect = effectData.createInstance();
            if (effect != null) {
                entity.addEffect(effect);
            }
        }

        if (entity instanceof Player player && (data.getFood() > 0 || data.getSaturation() > 0)) {
            FoodData foodData = player.getFoodData();
            foodData.eat(data.getFood(), data.getSaturation());
        }

        NeoForge.EVENT_BUS.post(new ConsumableUseEvent(entity, stack.copy(), index.getId(), index));

        ResourceLocation cooldownId = data.getCooldownCategory();
        if (cooldownId != null && data.getCooldown() > 0) {
            CustomItemCoolDowns cooldowns = entity instanceof Player player
                    ? player.getData(ModCapabilities.CUSTOM_COOLDOWN)
                    : null;
            if (cooldowns != null) {
                cooldowns.addCooldown(cooldownId, data.getCooldown());
            }
        }
    }

    @Override
    public boolean isSame(ItemStack stack1, ItemStack stack2) {
        return IConsumable.super.isSame(stack1, stack2);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        return Optional.of(new ConsumableTooltip(pStack));
    }
}
