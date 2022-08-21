package net.bettercombat.logic;

import net.bettercombat.api.WeaponAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class AttackHand {
    public final WeaponAttributes.Attack attack;
    public final boolean isOffHand;
    public final WeaponAttributes attributes;
    public final ItemStack itemStack;

    public AttackHand(WeaponAttributes.Attack attack, boolean isOffHand, WeaponAttributes attributes, ItemStack itemStack){
        this.attack = attack;
        this.isOffHand = isOffHand;
        this.attributes = attributes;
        this.itemStack = itemStack;
    }

    public double upswingRate() {
        return MathHelper.clamp(attack.upswing(), 0, 1);
    }
}
