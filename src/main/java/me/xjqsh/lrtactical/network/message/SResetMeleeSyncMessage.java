package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.init.ModCapabilities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SResetMeleeSyncMessage() implements CustomPacketPayload {
    public static final Type<SResetMeleeSyncMessage> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "reset_melee_sync")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SResetMeleeSyncMessage> STREAM_CODEC =
            StreamCodec.unit(new SResetMeleeSyncMessage());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SResetMeleeSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() ->
                context.player().getData(ModCapabilities.COMBAT_PROPERTIES).resetMeleeSync()
        );
    }
}
