package me.xjqsh.lrtactical.client.input;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import me.xjqsh.lrtactical.network.message.CCancelToggleConsumableUse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, modid = EquipmentMod.MOD_ID)
public class ConsumableInputHandler {
    private static boolean toggleUsing = false;
    private static InteractionHand toggleHand = InteractionHand.MAIN_HAND;
    /** 累计使用tick数（客户端跟踪，用于进度条显示） */
    private static int accumulatedTicks = 0;
    /** 记录开始TOGGLE使用时的物品，用于检测物品切换 */
    private static ItemStack toggleItem = ItemStack.EMPTY;
    /** 取消后的冷却期，防止右键取消后立即重新开始使用 */
    private static int cancelCooldown = 0;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.keyUse.matches(event.getKey(), event.getScanCode())) {
                handleUsePress(mc);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.keyUse.matchesMouse(event.getButton())) {
                if (handleUsePress(mc)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * 处理右键按下事件。
     * 如果正在TOGGLE使用中，再次按下右键表示取消。
     * @return true 如果事件应被取消（防止原版处理）
     */
    private static boolean handleUsePress(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) {
            return false;
        }

        // 如果正在TOGGLE使用中，再次按下右键表示取消
        if (toggleUsing && player.isUsingItem()) {
            ItemStack useItem = player.getUseItem();
            if (useItem.getItem() instanceof IConsumable) {
                ConsumableIndex index = LrTacticalAPI.getConsumableIndex(useItem).orElse(null);
                if (index != null && index.getData().isToggleUse()) {
                    // 取消TOGGLE使用
                    toggleUsing = false;
                    accumulatedTicks = 0;
                    toggleItem = ItemStack.EMPTY;
                    cancelCooldown = 5;
                    player.stopUsingItem();
                    PacketDistributor.sendToServer(new CCancelToggleConsumableUse());
                    // 强制释放使用键，防止handleKeybinds重新开始使用
                    mc.options.keyUse.setDown(false);
                    while (mc.options.keyUse.consumeClick()) {}
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 在游戏tick之前执行：
     * 1. 处理取消冷却期（防止取消后立即重新开始）
     * 2. 强制设置keyUse.isDown=true，阻止原版释放逻辑
     */
    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) {
            toggleUsing = false;
            accumulatedTicks = 0;
            cancelCooldown = 0;
            return;
        }

        // 处理取消冷却期：强制释放使用键，防止重新开始使用
        if (cancelCooldown > 0) {
            cancelCooldown--;
            mc.options.keyUse.setDown(false);
            while (mc.options.keyUse.consumeClick()) {}
            return;
        }

        if (!toggleUsing) {
            return;
        }

        // 检查物品是否仍然是同一个TOGGLE消耗品（玩家可能切换了物品栏）
        ItemStack currentItem = player.getItemInHand(toggleHand);
        if (!ItemStack.isSameItemSameComponents(currentItem, toggleItem)) {
            // 物品已切换，取消TOGGLE状态，强制释放使用键
            toggleUsing = false;
            accumulatedTicks = 0;
            toggleItem = ItemStack.EMPTY;
            mc.options.keyUse.setDown(false);
            while (mc.options.keyUse.consumeClick()) {}
            return;
        }

        if (player.isUsingItem()) {
            // 核心逻辑：强制设置使用键为按下状态，阻止原版的releaseUsingItem
            mc.options.keyUse.setDown(true);
        } else {
            // 使用完成或被中断，重置状态并释放使用键
            toggleUsing = false;
            accumulatedTicks = 0;
            toggleItem = ItemStack.EMPTY;
            mc.options.keyUse.setDown(false);
            while (mc.options.keyUse.consumeClick()) {}
        }
    }

    /**
     * 在游戏tick之后执行：跟踪累计使用tick数。
     */
    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        if (!toggleUsing) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        if (player.isUsingItem()) {
            accumulatedTicks++;
        }
    }

    /**
     * 标记进入TOGGLE使用状态。
     * 由ConsumableItem.use()在客户端调用。
     */
    public static void startToggleUsing(InteractionHand hand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            toggleUsing = true;
            toggleHand = hand;
            accumulatedTicks = 0;
            toggleItem = mc.player.getItemInHand(hand).copy();
            cancelCooldown = 0;
        }
    }

    /**
     * 重置TOGGLE使用状态。
     */
    public static void resetToggleUsing() {
        toggleUsing = false;
        accumulatedTicks = 0;
        toggleItem = ItemStack.EMPTY;
        cancelCooldown = 0;
    }

    /**
     * 是否处于TOGGLE使用状态。
     */
    public static boolean isToggleUsing() {
        return toggleUsing;
    }

    /**
     * 获取累计使用tick数（用于进度条显示）。
     */
    public static int getAccumulatedTicks() {
        return accumulatedTicks;
    }
}
