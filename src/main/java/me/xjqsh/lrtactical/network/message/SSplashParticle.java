package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SSplashParticle(
        BlockPos blockPos,
        int color
) implements CustomPacketPayload {

    public static final Type<SSplashParticle> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "splash_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SSplashParticle> STREAM_CODEC = StreamCodec.of(
            SSplashParticle::encode,
            SSplashParticle::decode
    );

    public static void encode(RegistryFriendlyByteBuf buf, SSplashParticle message) {
        buf.writeBlockPos(message.blockPos);
        buf.writeInt(message.color);
    }

    public static SSplashParticle decode(RegistryFriendlyByteBuf buf) {
        return new SSplashParticle(
                buf.readBlockPos(),
                buf.readInt()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SSplashParticle message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(SSplashParticle message) {
        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        var particleEngine = Minecraft.getInstance().particleEngine;
        if (player == null || level == null) {
            return;
        }
        int color = message.color;
        var randomsource = level.random;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        Vec3 vec3 = Vec3.atBottomCenterOf(message.blockPos);

        ParticleOptions particleoptions = ParticleTypes.EFFECT;

        for(int k2 = 0; k2 < 100; ++k2) {

            double d13 = randomsource.nextDouble() * 4.0D;
            double d19 = randomsource.nextDouble() * Math.PI * 2.0D;
            double d25 = Math.cos(d19) * d13;
            double d30 = 0.01D + randomsource.nextDouble() * 0.5D;
            double d31 = Math.sin(d19) * d13;
            Particle particle1 = particleEngine.createParticle(particleoptions, vec3.x + d25 * 0.1D, vec3.y + 0.3D, vec3.z + d31 * 0.1D, d25, d30, d31);
            if (particle1 != null) {
                float f2 = 0.75F + randomsource.nextFloat() * 0.25F;
                particle1.setColor(r * f2, g * f2, b * f2);
                particle1.setPower((float)d13);
            }
        }
    }
}
