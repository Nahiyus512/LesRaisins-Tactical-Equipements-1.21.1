package me.xjqsh.lrtactical.item;

import me.xjqsh.lrtactical.entity.GrenadeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DetonatorItem extends Item {
    public DetonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public void recordEntity(Entity entity, ItemStack detonatorStack) {
        UUID entityId = entity.getUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, detonatorStack, nbt -> nbt.putUUID("linked_entity", entityId));
    }

    public UUID getLinkedEntityId(ItemStack detonatorStack) {
        CustomData customData = detonatorStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        CompoundTag nbt = customData.copyTag();
        if (nbt.hasUUID("linked_entity")) {
            return nbt.getUUID("linked_entity");
        }
        return null;
    }

    public boolean detonate(ItemStack detonatorStack, Entity detonator) {
        UUID linkedId = getLinkedEntityId(detonatorStack);
        if (linkedId == null) {
            return false;
        }
        if (detonator.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(linkedId) instanceof GrenadeEntity grenadeEntity) {
                grenadeEntity.onDeath(null);
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack detonatorStack = pPlayer.getItemInHand(pUsedHand);
        if (detonate(detonatorStack, pPlayer)) {
            pPlayer.setItemInHand(pUsedHand, ItemStack.EMPTY);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("BOOM!").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.sidedSuccess(detonatorStack, pLevel.isClientSide());
        } else {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("- NO SIGNAL -").withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResultHolder.pass(detonatorStack);
    }
}
