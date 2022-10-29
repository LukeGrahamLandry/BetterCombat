package net.bettercombat.network;

import com.google.gson.Gson;
import net.bettercombat.BetterCombat;
import net.bettercombat.config.ServerConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class Packets {
    public static final class C2S_AttackRequest {
            public C2S_AttackRequest(int comboCount, boolean isSneaking, int selectedSlot, List<Entity> entities) {
                this(comboCount, isSneaking, selectedSlot, convertEntityList(entities));
            }

            private static int[] convertEntityList(List<Entity> entities) {
                int[] ids = new int[entities.size()];
                for (int i = 0; i < entities.size(); i++) {
                    ids[i] = entities.get(i).getEntityId();
                }
                return ids;
            }

            public static Identifier ID = new Identifier(BetterCombat.MODID, "c2s_request_attack");
            public static boolean UseVanillaPacket = true;
        private final int comboCount;
        private final boolean isSneaking;
        private final int selectedSlot;
        private final int[] entityIds;

        public C2S_AttackRequest(int comboCount, boolean isSneaking, int selectedSlot, int[] entityIds) {
            this.comboCount = comboCount;
            this.isSneaking = isSneaking;
            this.selectedSlot = selectedSlot;
            this.entityIds = entityIds;
        }

        public PacketByteBuf write() {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeInt(comboCount);
                buffer.writeBoolean(isSneaking);
                buffer.writeInt(selectedSlot);
                buffer.writeIntArray(entityIds);
                return buffer;
            }

            public static C2S_AttackRequest read(PacketByteBuf buffer) {
                int comboCount = buffer.readInt();
                boolean isSneaking = buffer.readBoolean();
                int selectedSlot = buffer.readInt();
                int[] ids = buffer.readIntArray();
                return new C2S_AttackRequest(comboCount, isSneaking, selectedSlot, ids);
            }

        public int comboCount() {
            return comboCount;
        }

        public boolean isSneaking() {
            return isSneaking;
        }

        public int selectedSlot() {
            return selectedSlot;
        }

        public int[] entityIds() {
            return entityIds;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            C2S_AttackRequest that = (C2S_AttackRequest) obj;
            return this.comboCount == that.comboCount &&
                    this.isSneaking == that.isSneaking &&
                    this.selectedSlot == that.selectedSlot &&
                    Objects.equals(this.entityIds, that.entityIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(comboCount, isSneaking, selectedSlot, entityIds);
        }

        @Override
        public String toString() {
            return "C2S_AttackRequest[" +
                    "comboCount=" + comboCount + ", " +
                    "isSneaking=" + isSneaking + ", " +
                    "selectedSlot=" + selectedSlot + ", " +
                    "entityIds=" + entityIds + ']';
        }

        }

    public static final class AttackAnimation {
            public static Identifier ID = new Identifier(BetterCombat.MODID, "attack_animation");
            public static String StopSymbol = "!STOP!";
        private final int playerId;
        private final boolean isOffHand;
        private final String animationName;
        private final float length;

        public AttackAnimation(int playerId, boolean isOffHand, String animationName, float length) {
            this.playerId = playerId;
            this.isOffHand = isOffHand;
            this.animationName = animationName;
            this.length = length;
        }

        public static AttackAnimation stop(int playerId) {
            return new AttackAnimation(playerId, false, StopSymbol, 0);
        }

            public PacketByteBuf write() {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeInt(playerId);
                buffer.writeBoolean(isOffHand);
                buffer.writeString(animationName);
                buffer.writeFloat(length);
                return buffer;
            }

            public static AttackAnimation read(PacketByteBuf buffer) {
                int playerId = buffer.readInt();
                boolean isOffHand = buffer.readBoolean();
                String animationName = buffer.readString();
                float length = buffer.readFloat();
                return new AttackAnimation(playerId, isOffHand, animationName, length);
            }

        public int playerId() {
            return playerId;
        }

        public boolean isOffHand() {
            return isOffHand;
        }

        public String animationName() {
            return animationName;
        }

        public float length() {
            return length;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            AttackAnimation that = (AttackAnimation) obj;
            return this.playerId == that.playerId &&
                    this.isOffHand == that.isOffHand &&
                    Objects.equals(this.animationName, that.animationName) &&
                    Float.floatToIntBits(this.length) == Float.floatToIntBits(that.length);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerId, isOffHand, animationName, length);
        }

        @Override
        public String toString() {
            return "AttackAnimation[" +
                    "playerId=" + playerId + ", " +
                    "isOffHand=" + isOffHand + ", " +
                    "animationName=" + animationName + ", " +
                    "length=" + length + ']';
        }

        }

    public static final class AttackSound {
            public static Identifier ID = new Identifier(BetterCombat.MODID, "attack_sound");
        private final double x;
        private final double y;
        private final double z;
        private final String soundId;
        private final float volume;
        private final float pitch;
        private final long seed;

        public AttackSound(double x, double y, double z, String soundId, float volume, float pitch, long seed) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.soundId = soundId;
            this.volume = volume;
            this.pitch = pitch;
            this.seed = seed;
        }

        public PacketByteBuf write() {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeDouble(x);
                buffer.writeDouble(y);
                buffer.writeDouble(z);
                buffer.writeString(soundId);
                buffer.writeFloat(volume);
                buffer.writeFloat(pitch);
                buffer.writeLong(seed);
                return buffer;
            }

            public static AttackSound read(PacketByteBuf buffer) {
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                String soundId = buffer.readString();
                float volume = buffer.readFloat();
                float pitch = buffer.readFloat();
                long seed = buffer.readLong();
                return new AttackSound(x, y, z, soundId, volume, pitch, seed);
            }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }

        public String soundId() {
            return soundId;
        }

        public float volume() {
            return volume;
        }

        public float pitch() {
            return pitch;
        }

        public long seed() {
            return seed;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            AttackSound that = (AttackSound) obj;
            return Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
                    Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
                    Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z) &&
                    Objects.equals(this.soundId, that.soundId) &&
                    Float.floatToIntBits(this.volume) == Float.floatToIntBits(that.volume) &&
                    Float.floatToIntBits(this.pitch) == Float.floatToIntBits(that.pitch) &&
                    this.seed == that.seed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, soundId, volume, pitch, seed);
        }

        @Override
        public String toString() {
            return "AttackSound[" +
                    "x=" + x + ", " +
                    "y=" + y + ", " +
                    "z=" + z + ", " +
                    "soundId=" + soundId + ", " +
                    "volume=" + volume + ", " +
                    "pitch=" + pitch + ", " +
                    "seed=" + seed + ']';
        }

        }

    public static class WeaponRegistrySync {
        public static Identifier ID = new Identifier(BetterCombat.MODID, "weapon_registry");
    }

    public static class ConfigSync {
        public static Identifier ID = new Identifier(BetterCombat.MODID, "config_sync");

        public static PacketByteBuf write(ServerConfig config) {
            Gson gson = new Gson();
            String json = gson.toJson(config);
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeString(json);
            return buffer;
        }

        public static ServerConfig read(PacketByteBuf buffer) {
            Gson gson = new Gson();
            String json = buffer.readString();
            ServerConfig config = gson.fromJson(json, ServerConfig.class);
            return config;
        }
    }
}
