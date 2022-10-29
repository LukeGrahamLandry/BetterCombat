package net.bettercombat.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public final class AttackHand {
    private final WeaponAttributes.Attack attack;
    private final ComboState combo;
    private final boolean isOffHand;
    private final WeaponAttributes attributes;
    private final ItemStack itemStack;

    public AttackHand(
            WeaponAttributes.Attack attack,
            ComboState combo,
            boolean isOffHand,
            WeaponAttributes attributes,
            ItemStack itemStack) {
        this.attack = attack;
        this.combo = combo;
        this.isOffHand = isOffHand;
        this.attributes = attributes;
        this.itemStack = itemStack;
    }

    public double upswingRate() {
        return MathHelper.clamp(attack.upswing(), 0, 1);
    }

    public WeaponAttributes.Attack attack() {
        return attack;
    }

    public ComboState combo() {
        return combo;
    }

    public boolean isOffHand() {
        return isOffHand;
    }

    public WeaponAttributes attributes() {
        return attributes;
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        AttackHand that = (AttackHand) obj;
        return Objects.equals(this.attack, that.attack) &&
                Objects.equals(this.combo, that.combo) &&
                this.isOffHand == that.isOffHand &&
                Objects.equals(this.attributes, that.attributes) &&
                Objects.equals(this.itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attack, combo, isOffHand, attributes, itemStack);
    }

    @Override
    public String toString() {
        return "AttackHand[" +
                "attack=" + attack + ", " +
                "combo=" + combo + ", " +
                "isOffHand=" + isOffHand + ", " +
                "attributes=" + attributes + ", " +
                "itemStack=" + itemStack + ']';
    }

}
