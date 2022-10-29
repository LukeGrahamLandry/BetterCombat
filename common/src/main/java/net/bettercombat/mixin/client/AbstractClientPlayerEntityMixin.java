package net.bettercombat.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.animation.FirstPersonAnimation;
import net.bettercombat.api.animation.FirstPersonAnimator;
import net.bettercombat.client.AnimationRegistry;
import net.bettercombat.client.PlayerAttackAnimatable;
import net.bettercombat.client.animation.*;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.mixin.LivingEntityAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Optional;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity implements PlayerAttackAnimatable, FirstPersonAnimator {
    private final AttackAnimationSubStack attackAnimation = new AttackAnimationSubStack(createAttackAdjustment());
    private final PoseSubStack mainHandBodyPose = new PoseSubStack(createPoseAdjustment(), true, true);
    private final PoseSubStack mainHandItemPose = new PoseSubStack(null, false, true);
    private final PoseSubStack offHandBodyPose = new PoseSubStack(null, true, false);
    private final PoseSubStack offHandItemPose = new PoseSubStack(null, false, true);
    private final ArrayList<ModifierLayer> additionalFirstPersonLayers = new ArrayList();

    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit(ClientWorld world, GameProfile profile, CallbackInfo ci) {
        AnimationStack stack = ((IAnimatedPlayer) this).getAnimationStack();
        stack.addAnimLayer(1, offHandItemPose.base);
        stack.addAnimLayer(2, offHandBodyPose.base);
        stack.addAnimLayer(3, mainHandItemPose.base);
        stack.addAnimLayer(4, mainHandBodyPose.base);
        stack.addAnimLayer(2000, attackAnimation.base);

        mainHandBodyPose.configure = this::updateAnimationByCurrentActivity;
        offHandBodyPose.configure = this::updateAnimationByCurrentActivity;
    }

    @Override
    public void updateAnimationsOnTick() {
        Object instance = (Object)this;
        PlayerEntity player = (PlayerEntity)instance;
        boolean isLeftHanded = isLeftHanded();

        // No pose during mining or item usage

        if (player.handSwinging || player.isUsingItem()) {
            offHandBodyPose.setPose(null, isLeftHanded);
            mainHandBodyPose.setPose(null, isLeftHanded);
            return;
        }

        // Restore auto body rotation upon swing - Fix issue #11

        if (getCurrentAnimation().isPresent() && getCurrentAnimation().get().isActive()) {
            ((LivingEntityAccessor)player).invokeTurnHead(player.getHeadYaw(), 0);
        }

        // Pose

        KeyframeAnimation newMainHandPose = null;
        WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(player.getMainHandStack());
        if (mainHandAttributes != null && mainHandAttributes.pose() != null) {
            newMainHandPose = AnimationRegistry.animations.get(mainHandAttributes.pose());
        }

        KeyframeAnimation newOffHandPose = null;
        if (PlayerAttackHelper.isDualWielding(player)) {
            WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(player.getOffHandStack());
            if (offHandAttributes != null && offHandAttributes.offHandPose() != null) {
                newOffHandPose = AnimationRegistry.animations.get(offHandAttributes.offHandPose());
            }
        }

        mainHandItemPose.setPose(newMainHandPose, isLeftHanded);
        offHandItemPose.setPose(newOffHandPose, isLeftHanded);

        if (!PlayerAttackHelper.isTwoHandedWielding(player)) {
            if (this.isWalking() || this.isSneaking()) {
                newMainHandPose = null;
                newOffHandPose = null;
            }
        }
        mainHandBodyPose.setPose(newMainHandPose, isLeftHanded);
        offHandBodyPose.setPose(newOffHandPose, isLeftHanded);
    }

    @Override
    public void playAttackAnimation(String name, boolean isOffHand, float length) {
        try {
            KeyframeAnimation animation = AnimationRegistry.animations.get(name);
            KeyframeAnimation.AnimationBuilder copy = animation.mutableCopy();
            updateAnimationByCurrentActivity(copy);
            copy.torso.fullyEnablePart(true);
            copy.head.pitch.setEnabled(false);
            float speed = ((float)animation.endTick) / length;
            boolean mirror = isOffHand;
            if(isLeftHanded()) {
                mirror = !mirror;
            }

            int fadeIn = copy.beginTick;
            attackAnimation.speed.speed = speed;
            attackAnimation.mirror.setEnabled(mirror);
            attackAnimation.base.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(fadeIn, Ease.INOUTSINE),
                    new CustomAnimationPlayer(copy.build(), 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdjustmentModifier createAttackAdjustment() {
        PlayerEntity player = (PlayerEntity)this;
        return new AdjustmentModifier((partName) -> {
            // System.out.println("Player pitch: " + player.getPitch(1));
            float rotationX = 0;
            float rotationY = 0;
            float rotationZ = 0;
            float offsetX = 0;
            float offsetY = 0;
            float offsetZ = 0;

            if (FirstPersonAnimation.isRenderingAttackAnimationInFirstPerson()) {
                float pitch = player.getPitch(1);
                pitch = (float) Math.toRadians(pitch);
                if ("rightArm".equals(partName) || "leftArm".equals(partName)) {
                    rotationX = pitch;
                } else {
                    return Optional.empty();
                }
            } else {
                float pitch = player.getPitch(1) / 2F;
                pitch = (float) Math.toRadians(pitch);
                if ("body".equals(partName)) {
                    rotationX = (-1F) * pitch;
                } else if ("rightArm".equals(partName) || "leftArm".equals(partName)) {
                    rotationX = pitch;
                } else if ("rightLeg".equals(partName) || "leftLeg".equals(partName)) {
                    rotationX = (-1F) * pitch;
                } else {
                    return Optional.empty();
                }
            }

            return Optional.of(new AdjustmentModifier.PartModifier(
                    new Vec3f(rotationX, rotationY, rotationZ),
                    new Vec3f(offsetX, offsetY, offsetZ))
            );
        });
    }

    private AdjustmentModifier createPoseAdjustment() {
        PlayerEntity player = (PlayerEntity)this;
        return new AdjustmentModifier((partName) -> {
            float rotationX = 0;
            float rotationY = 0;
            float rotationZ = 0;
            float offsetX = 0;
            float offsetY = 0;
            float offsetZ = 0;

            if (!FirstPersonRenderHelper.isRenderingFirstPersonPlayerModel) {
                if ("rightArm".equals(partName) || "leftArm".equals(partName)) {
                    if (!mainHandItemPose.lastAnimationUsesBodyChannel && player.isSneaking()) {
                        offsetY += 4;
                    }
                } else {
                    return Optional.empty();
                }
            }

            return Optional.of(new AdjustmentModifier.PartModifier(
                    new Vec3f(rotationX, rotationY, rotationZ),
                    new Vec3f(offsetX, offsetY, offsetZ))
            );
        });
    }

    private void updateAnimationByCurrentActivity(KeyframeAnimation.AnimationBuilder animation) {
        EntityPose pose = getPose();
        if (pose == EntityPose.STANDING) {
        } else if (pose == EntityPose.FALL_FLYING) {
        } else if (pose == EntityPose.SLEEPING) {
        } else if (pose == EntityPose.SWIMMING) {
            StateCollectionHelper.configure(animation.rightLeg, false, false);
            StateCollectionHelper.configure(animation.leftLeg, false, false);
        } else if (pose == EntityPose.SPIN_ATTACK) {
        } else if (pose == EntityPose.CROUCHING) {

        } else if (pose == EntityPose.DYING) {
        }
        if (isMounting()) {
            StateCollectionHelper.configure(animation.rightLeg, false, false);
            StateCollectionHelper.configure(animation.leftLeg, false, false);
        }
    }


    private boolean isWalking() {
        return !this.isDead() && (this.isSwimming() || Math.sqrt(Math.pow(this.getVelocity().x, 2) + Math.pow(this.getVelocity().z, 2)) > 0.03);
    }

    private boolean isMounting() {
        return this.getVehicle() != null;
    }

    public boolean isLeftHanded() {
        return this.getMainArm() == Arm.LEFT;
    }

    // PlayerAttackAnimatable

    @Override
    public void stopAttackAnimation() {
        IAnimation currentAnimation = attackAnimation.base.getAnimation();
        if (currentAnimation != null && currentAnimation instanceof KeyframeAnimationPlayer) {
            attackAnimation.base.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(5, Ease.INOUTSINE), null);
        }
    }

    @Override
    public Optional<IAnimation> getCurrentAnimation() {
        for (ModifierLayer layer: additionalFirstPersonLayers) {
            if (layer.isActive()) {
                return Optional.ofNullable(layer.getAnimation());
            }
        }
        return Optional.ofNullable(attackAnimation.base.getAnimation());
    }

    // FirstPersonAnimator

    public void addLayer(ModifierLayer layer) {
        additionalFirstPersonLayers.add(layer);
    }
}
