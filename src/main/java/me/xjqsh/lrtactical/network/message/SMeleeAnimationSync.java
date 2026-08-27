package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.event.MeleePreAttackEvent;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record SMeleeAnimationSync(
        int playerId,
        MeleeAction state,
        int actionCount,
        @Nullable ResourceLocation animationId
) implements CustomPacketPayload {
    public static final Type<SMeleeAnimationSync> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "melee_animation_sync")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SMeleeAnimationSync> STREAM_CODEC =
            StreamCodec.of(SMeleeAnimationSync::encode, SMeleeAnimationSync::decode);

    private static void encode(RegistryFriendlyByteBuf buf, SMeleeAnimationSync message) {
        buf.writeVarInt(message.playerId);
        buf.writeEnum(message.state);
        buf.writeVarInt(message.actionCount);
        buf.writeBoolean(message.animationId != null);
        if (message.animationId != null) {
            buf.writeResourceLocation(message.animationId);
        }
    }

    private static SMeleeAnimationSync decode(RegistryFriendlyByteBuf buf) {
        return new SMeleeAnimationSync(
                buf.readVarInt(),
                buf.readEnum(MeleeAction.class),
                buf.readVarInt(),
                buf.readBoolean() ? buf.readResourceLocation() : null
        );
    }

    public static void handle(SMeleeAnimationSync message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SMeleeAnimationSync message) {
        NeoForge.EVENT_BUS.post(
                new MeleePreAttackEvent(message.playerId, message.state, message.actionCount, message.animationId)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
