package net.bettercombat.logic;

import net.bettercombat.api.WeaponAttributes;

import java.io.InvalidObjectException;
import java.util.ArrayList;

public class WeaponAttributesHelper {
    public static WeaponAttributes override(WeaponAttributes a, WeaponAttributes b) {
        double attackRange = b.attackRange() > 0 ? b.attackRange() : a.attackRange();
        String pose = b.pose() != null ? b.pose() : a.pose();
        boolean isTwoHanded = b.isTwoHanded();
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
        return new WeaponAttributes(attackRange, pose, isTwoHanded, category, attacks);
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
}
