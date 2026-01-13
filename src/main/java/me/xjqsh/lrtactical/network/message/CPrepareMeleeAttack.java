package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.init.ModCapabilities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CPrepareMeleeAttack(
        MeleeAction action,
        Vec3 origin,
        Vec3 direction
) implements CustomPacketPayload {

    public static final Type<CPrepareMeleeAttack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "prepare_melee_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CPrepareMeleeAttack> STREAM_CODEC = StreamCodec.of(
            CPrepareMeleeAttack::encode,
            CPrepareMeleeAttack::decode
    );

    public static void encode(RegistryFriendlyByteBuf buf, CPrepareMeleeAttack message) {
        buf.writeEnum(message.action);
        buf.writeDouble(message.origin.x);
        buf.writeDouble(message.origin.y);
        buf.writeDouble(message.origin.z);
        buf.writeDouble(message.direction.x);
        buf.writeDouble(message.direction.y);
        buf.writeDouble(message.direction.z);
    }

    public static CPrepareMeleeAttack decode(RegistryFriendlyByteBuf buf) {
        return new CPrepareMeleeAttack(
                buf.readEnum(MeleeAction.class),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 服务端接到指令后，开始冷却读条
    public static void handle(CPrepareMeleeAttack message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            CombatProperties cap = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            cap.preAttack(message.action, message.origin, message.direction);
        });
    }
}
