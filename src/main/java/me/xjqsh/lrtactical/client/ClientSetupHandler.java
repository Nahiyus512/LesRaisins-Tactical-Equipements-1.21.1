package me.xjqsh.lrtactical.client;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.audio.SoundHandler;
import me.xjqsh.lrtactical.client.gui.overlay.InteractKeyTextOverlay;
import me.xjqsh.lrtactical.client.input.AttackKeys;
import me.xjqsh.lrtactical.client.overlay.UsingProgressOverlay;
import me.xjqsh.lrtactical.client.particle.SmokeCloudParticle;
import me.xjqsh.lrtactical.client.renderer.CoolDownDecorations;
import me.xjqsh.lrtactical.client.renderer.entity.ThrowableEntityRenderer;
import me.xjqsh.lrtactical.entity.EffectCloudGrenadeEntity;
import me.xjqsh.lrtactical.entity.GrenadeEntity;
import me.xjqsh.lrtactical.entity.SmokeGrenadeEntity;
import me.xjqsh.lrtactical.entity.StunGrenadeEntity;
import me.xjqsh.lrtactical.entity.sp.SpEffectCloudEntity;
import me.xjqsh.lrtactical.init.ModItems;
import me.xjqsh.lrtactical.init.ModParticleTypes;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
// import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = EquipmentMod.MOD_ID)
public class ClientSetupHandler {
    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GrenadeEntity.TYPE, ThrowableEntityRenderer::new);
        event.registerEntityRenderer(SmokeGrenadeEntity.TYPE, ThrowableEntityRenderer::new);
        event.registerEntityRenderer(StunGrenadeEntity.TYPE, ThrowableEntityRenderer::new);
        event.registerEntityRenderer(EffectCloudGrenadeEntity.TYPE, ThrowableEntityRenderer::new);

        event.registerEntityRenderer(SpEffectCloudEntity.TYPE, NoopRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.SMOKE_CLOUD.get(), SmokeCloudParticle::provider);
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(ModItems.THROWABLE.get(), new CoolDownDecorations());
    }

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "lr_using_progress"), new UsingProgressOverlay()::render);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(SoundHandler.get());
    }

    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        event.register(AttackKeys.NORMAL_ATTACK);
        event.register(AttackKeys.SPECIAL_ATTACK);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
        // 注册 HUD
        event.registerAbove(ResourceLocation.withDefaultNamespace("crosshair"), ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "lrt_interact_key_overlay"), new InteractKeyTextOverlay()::render);
    }
}

