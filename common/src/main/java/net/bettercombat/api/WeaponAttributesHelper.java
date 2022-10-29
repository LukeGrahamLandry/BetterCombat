package net.bettercombat.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.bettercombat.logic.ItemStackNBTWeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InvalidObjectException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class WeaponAttributesHelper {
    public static WeaponAttributes override(WeaponAttributes a, WeaponAttributes b) {
        double attackRange = b.attackRange() > 0 ? b.attackRange() : a.attackRange();
        String pose = b.pose() != null ? b.pose() : a.pose();
        String off_hand_pose = b.offHandPose() != null ? b.offHandPose() : a.offHandPose();
        Boolean isTwoHanded = b.two_handed() != null ? b.two_handed() : a.two_handed();
        String category = b.category() != null ? b.category() : a.category();
        WeaponAttributes.Attack[] attacks = a.attacks();
        if (b.attacks() != null && b.attacks().length > 0) {
            ArrayList<WeaponAttributes.Attack> overrideAttacks = new ArrayList<WeaponAttributes.Attack>();
            for(int i = 0; i < b.attacks().length; ++i) {
                WeaponAttributes.Attack base = (a.attacks() != null && a.attacks().length > i)
                        ? a.attacks()[i]
                        : new WeaponAttributes.Attack(null, null, 0, 0, 0, null, null, null);
                WeaponAttributes.Attack override = b.attacks()[i];
                WeaponAttributes.Attack attack = new WeaponAttributes.Attack(
                        override.conditions() != null ? override.conditions() : base.conditions(),
                        override.hitbox() != null ? override.hitbox() : base.hitbox(),
                        override.damageMultiplier() != 0 ? override.damageMultiplier() : base.damageMultiplier(),
                        override.angle() != 0 ? override.angle() : base.angle(),
                        override.upswing() != 0 ? override.upswing() : base.upswing(),
                        override.animation() != null ? override.animation() : base.animation(),
                        override.swingSound() != null ? override.swingSound() : base.swingSound(),
                        override.impactSound() != null ? override.impactSound() : base.impactSound());
                overrideAttacks.add(attack);
            }
            attacks = overrideAttacks.toArray(new WeaponAttributes.Attack[0]);
        }
        return new WeaponAttributes(attackRange, pose, off_hand_pose, isTwoHanded, category, attacks);
    }

    public static void validate(WeaponAttributes attributes) throws Exception {
        if (attributes.attacks() == null) {
            throw new InvalidObjectException("Undefined `attacks` array");
        }
        if (attributes.attacks().length == 0) {
            throw new InvalidObjectException("Empty `attacks` array");
        }
        int index = 0;
        for (WeaponAttributes.Attack attack : attributes.attacks()) {
            try {
                validate(attack);
            } catch(InvalidObjectException exception) {
                String message = "Invalid attack at index:" + index + " - " + exception.getMessage();
                throw new InvalidObjectException(message);
            }
            index += 1;
        }
    }

    private static void validate(WeaponAttributes.Attack attack) throws InvalidObjectException {
        if (attack.hitbox() == null) {
            throw new InvalidObjectException("Undefined `hitbox`");
        }
        if (attack.damageMultiplier() < 0) {
            throw new InvalidObjectException("Invalid `damage_multiplier`");
        }
        if (attack.angle() < 0) {
            throw new InvalidObjectException("Invalid `angle`");
        }
        if (attack.upswing() < 0) {
            throw new InvalidObjectException("Invalid `upswing`");
        }
        if (attack.animation() == null || attack.animation().length() == 0) {
            throw new InvalidObjectException("Undefined `animation`");
        }
    }

    public static final String nbtTag = "weapon_attributes";
    public static WeaponAttributes readFromNBT(ItemStack itemStack) {
        NbtCompound nbt = itemStack.getTag();
        ItemStackNBTWeaponAttributes attributedItemStack = (ItemStackNBTWeaponAttributes) ((Object)itemStack);
        String string = nbt.getString(nbtTag);
        if (string != null && !string.isEmpty() && !attributedItemStack.hasInvalidAttributes()) {
            WeaponAttributes cachedAttributes = attributedItemStack.getWeaponAttributes();
            if(cachedAttributes != null) {
                // System.out.println("NBT Attributes - Cache");
                return cachedAttributes;
            }
            Identifier itemId = Registry.ITEM.getId(itemStack.getItem());
            try {
                StringReader json = new StringReader(string);
                AttributesContainer container = decode(json);
                WeaponAttributes attributes = WeaponRegistry.resolveAttributes(itemId, container);
                if (attributes == null) {
                    attributedItemStack.setInvalidAttributes(true);
                }
                attributedItemStack.setWeaponAttributes(attributes);
                // System.out.println("NBT Attributes - Resolved");
                return attributes;
            } catch (Exception e) {
                System.err.println("Failed to resolve weapon attributes from ItemStack of item: " + itemId);
                System.err.println(e.getMessage());
                attributedItemStack.setInvalidAttributes(true);
            }
        }
        return null;
    }

    public static void writeToNBT(ItemStack itemStack, AttributesContainer container) {
        Identifier itemId = Registry.ITEM.getId(itemStack.getItem());
        ItemStackNBTWeaponAttributes attributedItemStack = (ItemStackNBTWeaponAttributes) ((Object)itemStack);
        NbtCompound nbt = itemStack.getTag();
        try {
            String json = encode(container);
            nbt.putString(nbtTag, json);
            attributedItemStack.setInvalidAttributes(false);
            attributedItemStack.setWeaponAttributes(null);
        } catch (Exception e) {
            System.err.println("Failed to write weapon attributes to ItemStack of item: " + itemId);
            System.err.println(e.getMessage());
        }
    }

    private static Type attributesContainerFileFormat = new TypeToken<AttributesContainer>() {}.getType();

    public static AttributesContainer decode(Reader reader) {
        Gson gson = new Gson();
        AttributesContainer container = gson.fromJson(reader, attributesContainerFileFormat);
        return container;
    }

    public static AttributesContainer decode(JsonReader json) {
        Gson gson = new Gson();
        AttributesContainer container = gson.fromJson(json, attributesContainerFileFormat);
        return container;
    }

    public static String encode(AttributesContainer container) {
        Gson gson = new Gson();
        return gson.toJson(container);
    }
}
