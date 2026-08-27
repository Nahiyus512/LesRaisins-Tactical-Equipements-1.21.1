package me.xjqsh.lrtactical.mixin.client;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 拦截松开右键时对 toggle 型消耗品的提前取消：
 * 手持 use_mode=toggle 的消耗品时，松开右键不会停止使用（需再次右键取消）。
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Redirect(
            method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"
            )
    )
    private void lrtactical$redirectReleaseUsingItem(MultiPlayerGameMode gameMode, Player player) {
        if (player != null && player.isUsingItem()) {
            ItemStack useItem = player.getUseItem();
            if (useItem.getItem() instanceof IConsumable
                    && LrTacticalAPI.getConsumableIndex(useItem)
                    .map(index -> index.getData().isToggleUse())
                    .orElse(false)) {
                return;
            }
        }
        gameMode.releaseUsingItem(player);
    }
}
