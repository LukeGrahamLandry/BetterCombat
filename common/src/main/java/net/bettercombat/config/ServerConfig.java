package net.bettercombat.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "server")
public class ServerConfig implements ConfigData {
    @Comment("Bypass damage receive throttling of LivingEntity from player attacks.")
    public boolean allow_fast_attacks = true;
    @Comment("Allows client-side target search and server-side attack request execution against currently mounted entity of the player")
    public boolean allow_attacking_mount = false;
    @Comment("Allows vanilla sweeping mechanic (and Sweeping Edge enchant) to work")
    public boolean allow_sweeping = true;
    @Comment("Allows client-side target search to ignore obstacles. WARNING! Setting this to `false` significantly increases the load on clients.")
    public boolean allow_attacking_thru_walls = false;
    @Comment("Applies movement speed multiplier while attacking. (Min: 0, Max: 1). Use `0` for a full stop while attacking. Use `1` for no movement speed penalty")
    public float movement_speed_while_attacking = 0.5F;
    @Comment("Determines if applying the movement speed multiplier while attacking is done smoothly or instantly")
    public boolean movement_speed_applied_smoothly = true;
    @Comment("Determines whether or not to apply movement speed reduction while attacking mounted")
    public boolean movement_speed_effected_while_mounting = false;
    @Comment("Multiplier for `attack_range`, during target lookup on both sides. " +
            "Large sized entities may be colliding with weapon hitbox, but center of entities can have bigger distance than `attack_range`")
    public float target_search_range_multiplier = 2F;
    @Comment("Total multiplier, (examples: +30% = 1.3, -30% = 0.7)")
    public float dual_wielding_attack_speed_multiplier = 1.2F;
    @Comment("Total multiplier, (examples: +30% = 1.3, -30% = 0.7)")
    public float dual_wielding_main_hand_damage_multiplier = 1F;
    @Comment("Total multiplier, (examples: +30% = 1.3, -30% = 0.7)")
    public float dual_wielding_off_hand_damage_multiplier = 1F;
    @Comment("Try to guess and apply a preset for items without weapon attributes data file")
    public boolean fallback_compatibility_enabled = true;
    @Comment("Allow printing the content of weapon attributes registry")
    public boolean weapon_registry_logging = false;
}
