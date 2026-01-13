package me.xjqsh.lrtactical.network.message;

import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SShieldDisable() implements CustomPacketPayload {

    public static final Type<SShieldDisable> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "shield_disable"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SShieldDisable> STREAM_CODEC = StreamCodec.of(
            SShieldDisable::encode,
            SShieldDisable::decode
    );

    public static void encode(RegistryFriendlyByteBuf buf, SShieldDisable message) {
    }

    public static SShieldDisable decode(RegistryFriendlyByteBuf buf) {
        return new SShieldDisable();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SShieldDisable message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SShieldDisable message) {
        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof FlashShieldItem) {
            if (IClientItemExtensions.of(player.getMainHandItem()).getCustomRenderer() instanceof AnimateGeoItemRenderer<?,?> renderer) {
                renderer.triggerAnimation(player.getMainHandItem(), "hard_shake");
            }
        }
    }
}
