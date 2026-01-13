package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.client.audio.ICustomSoundSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SCustomSound(
        SoundType soundType,
        ResourceLocation id,
        String key,
        Vec3 pos,
        float volume,
        float pitch
) implements CustomPacketPayload {

    public static final Type<SCustomSound> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "custom_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SCustomSound> STREAM_CODEC = StreamCodec.of(
            SCustomSound::encode,
            SCustomSound::decode
    );

    public enum SoundType {
        MELEE, THROWABLE
    }

    public static void encode(RegistryFriendlyByteBuf buf, SCustomSound message) {
        buf.writeEnum(message.soundType());
        buf.writeResourceLocation(message.id());
        buf.writeUtf(message.key());
        buf.writeDouble(message.pos().x);
        buf.writeDouble(message.pos().y);
        buf.writeDouble(message.pos().z);
        buf.writeFloat(message.volume());
        buf.writeFloat(message.pitch());
    }

    public static SCustomSound decode(RegistryFriendlyByteBuf buf) {
        return new SCustomSound(
                buf.readEnum(SoundType.class),
                buf.readResourceLocation(),
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SCustomSound message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SCustomSound message) {
        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        ICustomSoundSupplier supplier = switch (message.soundType()) {
            case MELEE -> LrTacticalAPI.getMeleeDisplay(message.id()).orElse(null);
            case THROWABLE -> LrTacticalAPI.getThrowableDisplay(message.id()).orElse(null);
            default -> null;
        };
        if (supplier != null) {
            ResourceLocation soundLocation = supplier.getSound(message.key());
            if (soundLocation != null) {
                level.playLocalSound(
                        message.pos.x, message.pos.y, message.pos.z,
                        SoundEvent.createVariableRangeEvent(soundLocation),
                        SoundSource.PLAYERS, message.volume, message.pitch, false
                );
            }
        }
    }
}
