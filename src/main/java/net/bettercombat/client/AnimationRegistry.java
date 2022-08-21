package net.bettercombat.client;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import net.minecraft.resource.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationRegistry {
    static final Logger LOGGER = LogManager.getLogger();
    public static Map<String, KeyframeAnimation> animations = new HashMap<>();

    public static void load(ResourceManager resourceManager) {
        var dataFolder = "attack_animations";
        for (var identifier : resourceManager.findResources(dataFolder, fileName -> new File(fileName).getPath().endsWith(".json"))) {
            try {
                var resource = resourceManager.getResource(identifier);
                List<KeyframeAnimation> readAnimations = AnimationSerializing.deserializeAnimation(resource.getInputStream());
                KeyframeAnimation animation = readAnimations.get(0);

                var id = identifier
                        .toString()
                        .replace(dataFolder + "/", "");
                id = id.substring(0, id.lastIndexOf('.'));
                AnimationRegistry.animations.put(id, animation);
            } catch (Exception e) {
                LOGGER.error("Failed to load animation " + identifier.toString());
                e.printStackTrace();
            }
        }
    }
}
