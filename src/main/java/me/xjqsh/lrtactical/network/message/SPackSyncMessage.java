package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.network.DataType;
import me.xjqsh.lrtactical.resource.CommonNetworkCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;


public record SPackSyncMessage(Map<DataType, Map<ResourceLocation, String>> cache) implements CustomPacketPayload {

    public static final Type<SPackSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "pack_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SPackSyncMessage> STREAM_CODEC = StreamCodec.of(
            SPackSyncMessage::encode,
            SPackSyncMessage::decode
    );

    public static void encode(RegistryFriendlyByteBuf buf, SPackSyncMessage message) {
        buf.writeMap(message.cache(), FriendlyByteBuf::writeEnum, (buf1, map) -> {
            buf1.writeMap(map, (b, r) -> b.writeResourceLocation(r), (b, s) -> b.writeUtf(s));
        });
    }

    public static SPackSyncMessage decode(RegistryFriendlyByteBuf buf) {
        var map = buf.readMap(buf1 -> buf1.readEnum(DataType.class), buf2 -> {
            return buf2.readMap(b -> b.readResourceLocation(), b -> b.readUtf());
        });
        return new SPackSyncMessage(map);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SPackSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> doSync(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void doSync(SPackSyncMessage message) {
        CommonNetworkCache.INSTANCE.fromNetwork(message.cache);
    }
}
