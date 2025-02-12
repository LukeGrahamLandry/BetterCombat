package net.bettercombat.utils;

import net.bettercombat.BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.network.Packets;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SoundHelper {
    private static Random rng = new Random();

    private static float rand(float min, float max){
        return min + rng.nextFloat() * (max - min);
    }

    public static void playSound(ServerWorld world, Entity entity, WeaponAttributes.Sound sound) {
        if (sound == null) {
            return;
        }
        
        try {
            float pitch = (sound.randomness() > 0)
                    ? rand(sound.pitch() - sound.randomness(), sound.pitch() + sound.randomness())
                    : sound.pitch();
            PacketByteBuf packet = new Packets.AttackSound(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    sound.id(),
                    sound.volume(),
                    pitch,
                    rng.nextLong())
                    .write();

            SoundEvent soundEvent = Registry.SOUND_EVENT.get(new Identifier(sound.id()));
            double distance = sound.volume() > 1.0f ? (double)(16.0f * sound.volume()) : 16.0;
            Vec3d origin = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
            PlayerLookup.around(world, origin, distance).forEach(serverPlayer -> {
                Identifier channel = Packets.AttackSound.ID;
                try {
                    if (ServerPlayNetworking.canSend(serverPlayer, channel)) {
                        ServerPlayNetworking.send(serverPlayer, channel, packet);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("Failed to play sound: " + sound.id());
            e.printStackTrace();
        }
    }

    public static List<String> soundKeys = Arrays.asList(
            "anchor_slam",
            "axe_slash",
            "claymore_swing",
            "claymore_stab",
            "claymore_slam",
            "dagger_slash",
            "double_axe_swing",
            "fist_punch",
            "glaive_slash_quick",
            "glaive_slash_slow",
            "hammer_slam",
            "katana_slash",
            "mace_slam",
            "mace_slash",
            "pickaxe_swing",
            "rapier_slash",
            "rapier_stab",
            "scythe_slash",
            "spear_stab",
            "staff_slam",
            "staff_slash",
            "staff_spin",
            "staff_stab",
            "sickle_slash",
            "sword_slash",
            "wand_swing"
    );

    public static void registerSounds() {
        for (String soundKey: soundKeys) {
            Identifier soundId = new Identifier(BetterCombat.MODID, soundKey);
            SoundEvent soundEvent = new SoundEvent(soundId);
            Registry.register(Registry.SOUND_EVENT, soundId, soundEvent);
        }
    }
}
