package me.xjqsh.lrtactical.handler;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.init.ModEnchantment;
import me.xjqsh.lrtactical.util.VectorUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CriticalHitEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        Registry<Enchantment> registry = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        @SuppressWarnings("deprecation")
        int level = EnchantmentHelper.getItemEnchantmentLevel(registry.getHolderOrThrow(ModEnchantment.BACKSTAB), player.getMainHandItem());
        if (level > 0) {
            Entity target = event.getTarget();
            Vec3 origin = player.getEyePosition();
            Vec3 positionVector = target.position().add(0, target.getBbHeight() / 2F, 0).subtract(origin);
            positionVector = new Vec3(positionVector.x, 0, positionVector.z).normalize();
            // 检查是否从背后攻击
            Vec3 targetForward = target.getForward();
            double angle = VectorUtil.angleBetween(positionVector, targetForward);
            if (angle <= 60) {
                // event.setResult(Event.Result.ALLOW);
                event.setDamageMultiplier(event.getDamageMultiplier() + level * 0.25f);
            }
        }
    }
}
