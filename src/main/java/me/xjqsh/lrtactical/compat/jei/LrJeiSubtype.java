package me.xjqsh.lrtactical.compat.jei;

import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class LrJeiSubtype {
    private LrJeiSubtype() {
    }

    public static ISubtypeInterpreter<ItemStack> getConsumableSubtype() {
        return new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack stack, UidContext uidContext) {
                return getLegacyStringSubtypeInfo(stack, uidContext);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
                if (stack.getItem() instanceof IConsumable iConsumable) {
                    return iConsumable.getId(stack).toString();
                }
                return "";
            }
        };
    }

    public static ISubtypeInterpreter<ItemStack> getMeleeSubtype() {
        return new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack stack, UidContext uidContext) {
                return getLegacyStringSubtypeInfo(stack, uidContext);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
                if (stack.getItem() instanceof IMeleeWeapon iMeleeWeapon) {
                    return iMeleeWeapon.getId(stack).toString();
                }
                return "";
            }
        };
    }

    public static ISubtypeInterpreter<ItemStack> getThrowableSubtype() {
        return new ISubtypeInterpreter<>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack stack, UidContext uidContext) {
                return getLegacyStringSubtypeInfo(stack, uidContext);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext uidContext) {
                if (stack.getItem() instanceof IThrowable iThrowable) {
                    return iThrowable.getId(stack).toString();
                }
                return "";
            }
        };
    }
}

