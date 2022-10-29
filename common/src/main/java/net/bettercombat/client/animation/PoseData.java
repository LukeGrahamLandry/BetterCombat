package net.bettercombat.client.animation;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import java.util.Objects;
import java.util.UUID;

public final class PoseData {
    private final UUID uuid;
    private final boolean isMirrored;

    public PoseData(UUID uuid, boolean isMirrored) {
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

    public UUID uuid() {
        return uuid;
    }

    public boolean isMirrored() {
        return isMirrored;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PoseData that = (PoseData) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                this.isMirrored == that.isMirrored;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, isMirrored);
    }

    @Override
    public String toString() {
        return "PoseData[" +
                "uuid=" + uuid + ", " +
                "isMirrored=" + isMirrored + ']';
    }

}
