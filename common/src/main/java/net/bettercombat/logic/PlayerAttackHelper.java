package net.bettercombat.logic;

import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.ComboState;
import net.bettercombat.api.WeaponAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.entity.EquipmentSlot.MAINHAND;

public class PlayerAttackHelper {
    public static float getDualWieldingAttackDamageMultiplier(PlayerEntity player, AttackHand hand) {
        return isDualWielding(player)
                ? (hand.isOffHand()
                    ? BetterCombat.config.dual_wielding_off_hand_damage_multiplier
                    : BetterCombat.config.dual_wielding_main_hand_damage_multiplier)
                : 1;
    }

    public static boolean shouldAttackWithOffHand(PlayerEntity player, int comboCount) {
        return PlayerAttackHelper.isDualWielding(player) && comboCount % 2 == 1;
    }

    public static boolean isDualWielding(PlayerEntity player) {
        WeaponAttributes mainAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
        WeaponAttributes offAttributes = WeaponRegistry.getAttributes(player.getOffHandStack());
        return mainAttributes != null && !mainAttributes.isTwoHanded()
                && offAttributes != null && !offAttributes.isTwoHanded();
    }

    public static boolean isTwoHandedWielding(PlayerEntity player) {
        WeaponAttributes mainAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
        if (mainAttributes != null) {
            return mainAttributes.isTwoHanded();
        }
        return false;
    }

    public static AttackHand getCurrentAttack(PlayerEntity player, int comboCount) {
        if (isDualWielding(player)) {
            boolean isOffHand = shouldAttackWithOffHand(player,comboCount);
            ItemStack itemStack = isOffHand
                    ? player.getOffHandStack()
                    : player.getMainHandStack();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            int handSpecificComboCount = ( (isOffHand && comboCount > 0) ? (comboCount - 1) : (comboCount) ) / 2;
            AttackSelection attackSelection = selectAttack(handSpecificComboCount, attributes, player, isOffHand);
            WeaponAttributes.Attack attack = attackSelection.attack;
            ComboState combo = attackSelection.comboState;
            return new AttackHand(attack, combo, isOffHand, attributes, itemStack);
        } else {
            ItemStack itemStack = player.getMainHandStack();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            if (attributes != null) {
                AttackSelection attackSelection = selectAttack(comboCount, attributes, player, false);
                WeaponAttributes.Attack attack = attackSelection.attack;
                ComboState combo = attackSelection.comboState;
                return new AttackHand(attack, combo, false, attributes, itemStack);
            }
        }
        return null;
    }

    private static final class AttackSelection {
        private final WeaponAttributes.Attack attack;
        private final ComboState comboState;

        private AttackSelection(WeaponAttributes.Attack attack, ComboState comboState) {
            this.attack = attack;
            this.comboState = comboState;
        }

        public WeaponAttributes.Attack attack() {
            return attack;
        }

        public ComboState comboState() {
            return comboState;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            AttackSelection that = (AttackSelection) obj;
            return Objects.equals(this.attack, that.attack) &&
                    Objects.equals(this.comboState, that.comboState);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attack, comboState);
        }

        @Override
        public String toString() {
            return "AttackSelection[" +
                    "attack=" + attack + ", " +
                    "comboState=" + comboState + ']';
        }
    }

    private static AttackSelection selectAttack(int comboCount, WeaponAttributes attributes, PlayerEntity player, boolean isOffHandAttack) {
        WeaponAttributes.Attack[] attacks = attributes.attacks();
        attacks = Arrays.stream(attacks)
                .filter(attack ->
                        attack.conditions() == null
                        || attack.conditions().length == 0
                        || evaluateConditions(attack.conditions(), player, isOffHandAttack)
                )
                .toArray(WeaponAttributes.Attack[]::new);
        if (comboCount < 0) {
            comboCount = 0;
        }
        int index = comboCount % attacks.length;
        return new AttackSelection(attacks[index], new ComboState(index + 1, attacks.length));
    }

    private static boolean evaluateConditions(WeaponAttributes.Condition[] conditions, PlayerEntity player, boolean isOffHandAttack) {
        return Arrays.stream(conditions).allMatch(condition -> evaluateCondition(condition, player, isOffHandAttack));
    }

    private static boolean evaluateCondition(WeaponAttributes.Condition condition, PlayerEntity player, boolean isOffHandAttack) {
        if (condition == null) {
            return true;
        }
        if (condition == WeaponAttributes.Condition.NOT_DUAL_WIELDING) {
            return !isDualWielding(player);
        } else if (condition == WeaponAttributes.Condition.DUAL_WIELDING_ANY) {
            return isDualWielding(player);
        } else if (condition == WeaponAttributes.Condition.DUAL_WIELDING_SAME) {
            return isDualWielding(player) &&
                    (player.getMainHandStack().getItem() == player.getOffHandStack().getItem());
        } else if (condition == WeaponAttributes.Condition.DUAL_WIELDING_SAME_CATEGORY) {
            if (!isDualWielding(player)) {
                return false;
            }
            WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
            WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(player.getOffHandStack());
            if (mainHandAttributes.category() == null
                    || mainHandAttributes.category().isEmpty()
                    || offHandAttributes.category() == null
                    || offHandAttributes.category().isEmpty()) {
                return false;
            }
            return mainHandAttributes.category().equals(offHandAttributes.category());
        } else if (condition == WeaponAttributes.Condition.NO_OFFHAND_ITEM) {
            ItemStack offhandStack = player.getOffHandStack();
            if (offhandStack == null || offhandStack.isEmpty()) {
                {
                    return true;
                }
            }
            return false;
        } else if (condition == WeaponAttributes.Condition.OFF_HAND_SHIELD) {
            ItemStack offhandStack = player.getOffHandStack();
            if (offhandStack != null || offhandStack.getItem() instanceof ShieldItem) {
                {
                    return true;
                }
            }
            return false;
        } else if (condition == WeaponAttributes.Condition.MAIN_HAND_ONLY) {
            return !isOffHandAttack;
        } else if (condition == WeaponAttributes.Condition.OFF_HAND_ONLY) {
            return isOffHandAttack;
        }
        return true;
    }

    public static void setAttributesForOffHandAttack(PlayerEntity player, boolean useOffHand) {
        ItemStack mainHandStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();
        ItemStack add;
        ItemStack remove;
        if (useOffHand) {
            remove = mainHandStack;
            add = offHandStack;
        } else {
            remove = offHandStack;
            add = mainHandStack;
        }
        if (remove != null) {
            player.getAttributes().removeModifiers(remove.getAttributeModifiers(MAINHAND));
        }
        if (add != null) {
            player.getAttributes().addTemporaryModifiers(add.getAttributeModifiers(MAINHAND));
        }
    }
}
