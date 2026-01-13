package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.config.ServerConfig;
import me.xjqsh.lrtactical.init.ModCapabilities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record CMeleeAttackRequest(
        MeleeAction action,
        int[] entityIds
) implements CustomPacketPayload {

    public static final Type<CMeleeAttackRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "melee_attack_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CMeleeAttackRequest> STREAM_CODEC = StreamCodec.of(
            CMeleeAttackRequest::encode,
            CMeleeAttackRequest::decode
    );

    public CMeleeAttackRequest(MeleeAction action, List<Entity> entities) {
        this(action, toList(entities));
    }

    private static int[] toList(List<Entity> entities) {
        return entities.stream().mapToInt(Entity::getId).limit(ServerConfig.MELEE_MAX_TARGET_PER_PACKET.get()).toArray();
    }

    public static void encode(RegistryFriendlyByteBuf buf, CMeleeAttackRequest message) {
        buf.writeEnum(message.action);
        buf.writeVarIntArray(message.entityIds);
    }

    public static CMeleeAttackRequest decode(RegistryFriendlyByteBuf buf) {
        MeleeAction action = buf.readEnum(MeleeAction.class);
        int[] ids = buf.readVarIntArray();
        return new CMeleeAttackRequest(action, ids);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CMeleeAttackRequest message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (message.entityIds.length > ServerConfig.MELEE_MAX_TARGET_PER_PACKET.get()) {
                EquipmentMod.LOGGER.info(
                        "Player {} tried to attack too many entities at once: {}! Ignoring.",
                        player.getName().getString(),
                        message.entityIds.length
                );
                return;
            }

            List<Entity> entities = new ArrayList<>();
            for (int entityId : message.entityIds()) {
                Entity entity = player.level().getEntity(entityId);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            CombatProperties cap = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            cap.postAttack(message.action, entities);
        });
    }
}