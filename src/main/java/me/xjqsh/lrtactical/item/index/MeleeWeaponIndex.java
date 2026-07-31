package me.xjqsh.lrtactical.item.index;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.index.ICustomItemIndex;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponData;
import me.xjqsh.lrtactical.item.melee.MeleeWeaponType;
import me.xjqsh.lrtactical.util.DefaultAttrUUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class MeleeWeaponIndex<T extends MeleeWeaponData> implements ICustomItemIndex {
    private final MeleeWeaponType<T> type;
    private final Item baseItem;
    private final T data;
    private final ResourceLocation id;
    private final String name;
    private final String tooltip;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final ItemAttributeModifiers itemAttributeModifiers;
    private List<FormattedCharSequence> desc;


    private MeleeWeaponIndex(MeleeWeaponType<T> type, T data, String name, String tooltip,
                             ResourceLocation id, Item baseItem) {
        this.type = type;
        this.baseItem = baseItem;
        this.data = data;
        this.id = id;
        this.name = name;
        this.tooltip = tooltip;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        ItemAttributeModifiers.Builder itemAttributesBuilder = ItemAttributeModifiers.builder();
        for (var entry : data.getRawAttributes().getAttributes()) {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(entry.id());
            if (attribute == null) {
                EquipmentMod.LOGGER.error("Unknown attribute {} for melee weapon {}", entry.id(), id);
                continue;
            }
            AttributeModifier modifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "melee_modifier_" + entry.id().getPath()),
                    entry.amount(),
                    entry.operation()
            );
            builder.put(attribute, modifier);
            itemAttributesBuilder.add(
                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute),
                    modifier,
                    EquipmentSlotGroup.MAINHAND
            );
        }
        defaultModifiers = builder.build();
        itemAttributeModifiers = itemAttributesBuilder.build();
    }

    @Nullable
    public static <T extends MeleeWeaponData> MeleeWeaponIndex<T> deserialize(
            @NotNull MeleeWeaponType<T> type, JsonElement data, String name, String tooltip, ResourceLocation id, Item baseItem
    ) {
        T meleeData = type.serializer().parse(data);
        if (meleeData == null) {
            return null;
        }
        return new MeleeWeaponIndex<>(type, meleeData, name, tooltip, id, baseItem);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultModifiers() {
        return defaultModifiers;
    }

    public void applyAttributeModifiers(ItemAttributeModifierEvent event) {
        for (ItemAttributeModifiers.Entry entry : itemAttributeModifiers.modifiers()) {
            event.replaceModifier(entry.attribute(), entry.modifier(), entry.slot());
        }
    }

    public T getData() {
        return data;
    }

    public MeleeWeaponType<T> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getTooltip() {
        return tooltip;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(baseItem);
        if (stack.getItem() instanceof IMeleeWeapon iMeleeWeapon) {
            iMeleeWeapon.setId(stack, this.getId());
        }
        trySetEnchantableComponent(stack, data.getEnchantmentValue());
        if (data.getMaxDurability() > 0) {
            stack.set(DataComponents.MAX_DAMAGE, data.getMaxDurability());
            stack.set(DataComponents.DAMAGE, 0);
        }
        return stack;
    }

    public static void trySetEnchantableComponent(ItemStack stack, int enchantmentValue) {
        if (enchantmentValue <= 0) {
            return;
        }
        try {
            Class<?> enchantableClass = Class.forName("net.minecraft.world.item.enchantment.Enchantable");
            Object enchantable = enchantableClass.getConstructor(int.class).newInstance(enchantmentValue);
            Method setMethod = ItemStack.class.getMethod("set", DataComponentType.class, Object.class);
            for (DataComponentType<?> candidate : getEnchantableComponentCandidates()) {
                try {
                    setMethod.invoke(stack, candidate, enchantable);
                    return;
                } catch (ReflectiveOperationException | IllegalArgumentException | ClassCastException ignored) {
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static List<DataComponentType<?>> getEnchantableComponentCandidates() {
        ArrayList<DataComponentType<?>> candidates = new ArrayList<>();
        addComponentCandidate(candidates, "ENCHANTABLE");
        addComponentCandidate(candidates, "ENCHANTABILITY");
        addComponentCandidate(candidates, "ENCHANTMENT_VALUE");
        for (Field field : DataComponents.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!DataComponentType.class.isAssignableFrom(field.getType())) {
                continue;
            }
            String name = field.getName();
            if (!name.contains("ENCHANT")) {
                continue;
            }
            try {
                Object value = field.get(null);
                if (value instanceof DataComponentType<?> type && !candidates.contains(type)) {
                    candidates.add(type);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return candidates;
    }

    private static void addComponentCandidate(List<DataComponentType<?>> candidates, String fieldName) {
        try {
            Field field = DataComponents.class.getField(fieldName);
            if (!Modifier.isStatic(field.getModifiers())) {
                return;
            }
            Object value = field.get(null);
            if (value instanceof DataComponentType<?> type && !candidates.contains(type)) {
                candidates.add(type);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    public int getMaxDurability() {
        return data.getMaxDurability();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public Item getBaseItem() {
        return baseItem;
    }

    @Override
    public String getDescriptionId() {
        return name;
    }
}
