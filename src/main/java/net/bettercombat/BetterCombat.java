package net.bettercombat;

import net.bettercombat.config.FallbackConfig;
import net.bettercombat.config.ServerConfig;
import net.bettercombat.logic.WeaponAttributesFallback;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.network.ServerNetwork;
import net.bettercombat.utils.SoundHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ServerResourceManager;
import net.tinyconfig.ConfigManager;

public class BetterCombat implements ModInitializer {
    public static final String MODID = "bettercombat";
    public static ServerConfig config;
    private static FallbackConfig fallbackDefault = FallbackConfig.createDefault();
    public static ConfigManager<FallbackConfig> fallbackConfig = new ConfigManager<FallbackConfig>
            ("fallback_compatibility", fallbackDefault)
            .builder()
            .setDirectory(MODID)
            .sanitize(true)
            .build();

    @Override
    public void onInitialize() {
        // TODO
        // AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        // Intuitive way to load a config :)
        config = new ServerConfig(); // AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig().server;
        loadFallbackConfig();

        ServerNetwork.initializeHandlers();
        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
            ResourceManager resourceManger = minecraftServer.getDataPackRegistries().getResourceManager();
            WeaponRegistry.loadAttributes(resourceManger);
            if (config.fallback_compatibility_enabled) {
                WeaponAttributesFallback.initialize();
            }
            WeaponRegistry.encodeRegistry();
        });
        SoundHelper.registerSounds();
    }

    private void loadFallbackConfig() {
        fallbackConfig.load();
        if (fallbackConfig.currentConfig.schema_version < fallbackDefault.schema_version) {
            fallbackConfig.currentConfig = FallbackConfig.migrate(fallbackConfig.currentConfig, FallbackConfig.createDefault());
        }
        fallbackConfig.save();
    }
}