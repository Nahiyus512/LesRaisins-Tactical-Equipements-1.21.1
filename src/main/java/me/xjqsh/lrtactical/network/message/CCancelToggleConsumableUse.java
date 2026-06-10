package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.init.ModCapabilities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CCancelToggleConsumableUse implements CustomPacketPayload {
    public static final Type<CCancelToggleConsumableUse> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "cancel_toggle_consumable"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CCancelToggleConsumableUse> STREAM_CODEC = StreamCodec.of(
            (buf, msg) -> {},
            buf -> new CCancelToggleConsumableUse()
    );

    public CCancelToggleConsumableUse() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CCancelToggleConsumableUse message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack useItem = player.getUseItem();
                if (useItem.getItem() instanceof IConsumable && LrTacticalAPI.getConsumableIndex(useItem)
                        .map(index -> index.getData().isToggleUse())
                        .orElse(false)) {
                    // 停止使用但不触发完成效果
                    player.stopUsingItem();
                    // 重置服务端的累计使用时间
                    player.getData(ModCapabilities.COMBAT_PROPERTIES).setToggleUseTicks(0);
                }
            }
        });
    }
}