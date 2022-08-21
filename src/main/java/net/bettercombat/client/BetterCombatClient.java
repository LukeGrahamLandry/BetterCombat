package net.bettercombat.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.bettercombat.BetterCombat;
import net.bettercombat.client.animation.FirstPersonRenderHelper;
import net.bettercombat.config.ClientConfig;
import net.bettercombat.config.ClientConfigWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BetterCombatClient implements ClientModInitializer {
    public static ClientConfig config;
    public static KeyBinding feintKeyBinding;
    public static KeyBinding toggleMineKeyBinding;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ClientConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        // Intuitive way to load a config :)
        config = AutoConfig.getConfigHolder(ClientConfigWrapper.class).getConfig().client;

        ClientNetwork.initializeHandlers();
        WeaponAttributeTooltip.initialize();
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            AnimationRegistry.load(resourceManager);
        });
        registerKeyBindings();

        if (FabricLoader.getInstance().isModLoaded("firstperson")) {
            FirstPersonRenderHelper.isFeatureEnabled = false;
        }

        FabricModelPredicateProviderRegistry.register(new Identifier(BetterCombat.MODID, "loaded"), (stack, world, entity) -> {
            return 1.0F;
        });
    }

    private void registerKeyBindings() {
        feintKeyBinding = new KeyBinding(
                "keybinds.bettercombat.feint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "Better Combat");
        KeyBindingHelper.registerKeyBinding(feintKeyBinding);
        toggleMineKeyBinding = new KeyBinding(
                "keybinds.bettercombat.toggle_mine_with_weapons",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "Better Combat");
        KeyBindingHelper.registerKeyBinding(toggleMineKeyBinding);
    }
}
