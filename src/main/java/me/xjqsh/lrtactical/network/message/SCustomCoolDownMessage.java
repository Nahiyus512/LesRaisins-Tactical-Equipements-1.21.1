package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.capability.CustomItemCoolDowns;
import me.xjqsh.lrtactical.init.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SCustomCoolDownMessage(ResourceLocation id, int duration) implements CustomPacketPayload {

    public static final Type<SCustomCoolDownMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "custom_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SCustomCoolDownMessage> STREAM_CODEC = StreamCodec.of(
            SCustomCoolDownMessage::encode,
            SCustomCoolDownMessage::decode
    );

    public static void encode(RegistryFriendlyByteBuf buf, SCustomCoolDownMessage message) {
        buf.writeResourceLocation(message.id);
        buf.writeInt(message.duration);
    }

    public static SCustomCoolDownMessage decode(RegistryFriendlyByteBuf buf) {
        return new SCustomCoolDownMessage(buf.readResourceLocation(), buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SCustomCoolDownMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SCustomCoolDownMessage message) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CustomItemCoolDowns coolDownCapability = player.getData(ModCapabilities.CUSTOM_COOLDOWN);
        if (message.duration() == 0) {
            coolDownCapability.removeCooldown(message.id());
        } else {
            coolDownCapability.addCooldown(message.id(), message.duration());
        }
    }
}