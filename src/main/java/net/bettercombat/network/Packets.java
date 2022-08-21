package net.bettercombat.network;

import com.google.gson.Gson;
import net.bettercombat.BetterCombat;
import net.bettercombat.config.ServerConfig;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public class Packets {
    public static class C2S_AttackRequest {
        public final int comboCount;
        public final boolean isSneaking;
        public final int[] entityIds;
        public C2S_AttackRequest(int comboCount, boolean isSneaking, List<Entity> entities) {
            this.comboCount = comboCount;
            this.isSneaking = isSneaking;
            this.entityIds = convertEntityList(entities);
        }

        public C2S_AttackRequest(int comboCount, boolean isSneaking, int[] entities) {
            this.comboCount = comboCount;
            this.isSneaking = isSneaking;
            this.entityIds = entities;
        }

        private static int[] convertEntityList(List<Entity> entities) {
            int[] ids = new int[entities.size()];
            for(int i = 0; i < entities.size(); i++) {
                ids[i] = entities.get(i).getEntityId();
            }
            return ids;
        }

        public static Identifier ID = new Identifier(BetterCombat.MODID, "c2s_request_attack");
        public static boolean UseVanillaPacket = true;
        public PacketByteBuf write() {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(comboCount);
            buffer.writeBoolean(isSneaking);
            buffer.writeIntArray(entityIds);
            return buffer;
        }

        public static C2S_AttackRequest read(PacketByteBuf buffer) {
            int comboCount = buffer.readInt();
            boolean isSneaking = buffer.readBoolean();
            int[] ids = buffer.readIntArray();
            return new C2S_AttackRequest(comboCount, isSneaking, ids);
        }
    }

    public static class AttackAnimation {
        public final int playerId;
        public final boolean isOffHand;
        public final String animationName;
        public final float length;

        public AttackAnimation(int playerId, boolean isOffHand, String animationName, float length) {
            this.playerId = playerId;
            this.isOffHand = isOffHand;
            this.animationName = animationName;
            this.length = length;
        }
        public static Identifier ID = new Identifier(BetterCombat.MODID, "attack_animation");
        public static String StopSymbol = "!STOP!";
        public static AttackAnimation stop(int playerId) { return new AttackAnimation(playerId, false, StopSymbol, 0); }

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
    }

    public static class AttackSound {
        public final double x;
        public final double y;
        public final double z;
        public final String soundId;
        public final float volume;
        public final float pitch;
        public final long seed;
        public AttackSound(double x, double y, double z, String soundId, float volume, float pitch, long seed){

            this.x = x;
            this.y = y;
            this.z = z;
            this.soundId = soundId;
            this.volume = volume;
            this.pitch = pitch;
            this.seed = seed;
        }

        public static Identifier ID = new Identifier(BetterCombat.MODID, "attack_sound");
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
