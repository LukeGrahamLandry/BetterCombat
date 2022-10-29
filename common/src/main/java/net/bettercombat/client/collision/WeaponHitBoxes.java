package net.bettercombat.client.collision;

import net.bettercombat.api.WeaponAttributes;
import net.minecraft.util.math.Vec3d;

public class WeaponHitBoxes {
    public static Vec3d createHitbox(WeaponAttributes.HitBoxShape direction, double attackRange, boolean isSpinAttack) {
        if (direction == WeaponAttributes.HitBoxShape.FORWARD_BOX) {
            return new Vec3d(attackRange * 0.5, attackRange * 0.5, attackRange);
        } else if (direction == WeaponAttributes.HitBoxShape.VERTICAL_PLANE) {
            float zMultiplier = isSpinAttack ? 2 : 1;
            return new Vec3d(attackRange / 3.0, attackRange * 2.0, attackRange * zMultiplier);
        } else if (direction == WeaponAttributes.HitBoxShape.HORIZONTAL_PLANE) {
            float zMultiplier = isSpinAttack ? 2 : 1;
            return new Vec3d(attackRange * 2.0, attackRange / 3.0, attackRange * zMultiplier);
        }
        return null;
    }
}
