package net.bettercombat.client.animation;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import java.util.UUID;

public class PoseData {
    private final UUID uuid;
    private final boolean isMirrored;

    public PoseData(UUID uuid, boolean isMirrored){
        this.uuid = uuid;
        this.isMirrored = isMirrored;
    }
    public static PoseData from(KeyframeAnimation animation, boolean isMirrored) {
        UUID uuid = null;
        if (animation != null) {
            uuid = animation.getUuid();
        }
        return new PoseData(uuid, isMirrored);
    }
}
