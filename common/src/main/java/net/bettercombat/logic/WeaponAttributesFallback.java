package net.bettercombat.logic;

import com.google.common.collect.Multimap;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttributesContainer;
import net.bettercombat.config.FallbackConfig;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeaponAttributesFallback {
    public static void initialize() {
        FallbackConfig config = BetterCombat.fallbackConfig.value;
        for(Identifier itemId: Registry.ITEM.getIds()) {
            Item item = Registry.ITEM.get(itemId);
            if (!hasAttributeModifier(item, EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    || matches(itemId.toString(), config.blacklist_item_id_regex)) {
                // Skipping items without attack damage attribute
                continue;
            }
            for (FallbackConfig.CompatibilitySpecifier fallbackOption: config.fallback_compatibility) {
                // If - no registration & matches regex
                if (WeaponRegistry.getAttributes(itemId) == null
                        && matches(itemId.toString(), fallbackOption.item_id_regex)) {
                    AttributesContainer container = WeaponRegistry.containers.get(new Identifier(fallbackOption.weapon_attributes));
                    // If assignable attributes are known
                    if (container != null) {
                        WeaponRegistry.resolveAndRegisterAttributes(itemId, container);
                        break; // No more registration attempts for this item id
                    }
                }
            }
        }
    }

    private static boolean hasAttributeModifier(Item item, EntityAttribute searchedAttribute) {
        Identifier searchedAttributeId = Registry.ATTRIBUTE.getId(searchedAttribute);
        Multimap<EntityAttribute, EntityAttributeModifier> attributes = item.getAttributeModifiers(EquipmentSlot.MAINHAND);
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry: attributes.entries()) {
            EntityAttribute attribute = entry.getKey();
            Identifier attributeId = Registry.ATTRIBUTE.getId(attribute);
            if (attributeId.equals(searchedAttributeId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String subject, String nullableRegex) {
        if (subject == null) {
            return false;
        }
        if (nullableRegex == null || nullableRegex.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(nullableRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
}
