package net.bettercombat.mixin.client.firstpersonrender;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import net.bettercombat.client.PlayerAttackAnimatable;
import net.bettercombat.client.animation.FirstPersonRenderHelper;
import net.bettercombat.client.animation.IExtendedAnimation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"), cancellable = true)
    private void dontRenderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
                                ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (!FirstPersonRenderHelper.isFeatureEnabled) {
            return;
        }
        Optional<IAnimation> currentAnimation = ((PlayerAttackAnimatable) player).getCurrentAnimation();
        if (currentAnimation.isPresent()) {
            boolean isActive = currentAnimation.get().isActive();
            if (currentAnimation.get() instanceof IExtendedAnimation extendedAnimation) {
                isActive = extendedAnimation.isActiveInFirstPerson();
            }

            if (isActive) {
                ci.cancel();
            }
        }
    }
}
