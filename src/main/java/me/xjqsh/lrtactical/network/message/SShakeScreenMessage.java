package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.ClientEventsHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SShakeScreenMessage(double time, double radius, double amplitude, Vec3 position) implements CustomPacketPayload {

    public static final Type<SShakeScreenMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "shake_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SShakeScreenMessage> STREAM_CODEC = StreamCodec.of(
            SShakeScreenMessage::encode,
            SShakeScreenMessage::decode
    );

    public static void encode(RegistryFriendlyByteBuf buffer, SShakeScreenMessage message) {
        buffer.writeDouble(message.time);
        buffer.writeDouble(message.radius);
        buffer.writeDouble(message.amplitude);
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
    }

    public static SShakeScreenMessage decode(RegistryFriendlyByteBuf buffer) {
        return new SShakeScreenMessage(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SShakeScreenMessage message, IPayloadContext context) {
        context.enqueueWork(() -> ClientEventsHandler.handleShakeClient(
                message.time, message.radius, message.amplitude, message.position
        ));
    }
}
