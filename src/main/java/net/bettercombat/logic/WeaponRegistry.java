package net.bettercombat.logic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttributesContainer;
import net.bettercombat.api.WeaponAttributes;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponRegistry {
    static final Logger LOGGER = LogManager.getLogger();
    static Map<Identifier, WeaponAttributes> registrations = new HashMap();
    static Map<Identifier, AttributesContainer> containers = new HashMap();

    public static void register(Identifier itemId, WeaponAttributes attributes) {
        registrations.put(itemId, attributes);
    }

    public static WeaponAttributes getAttributes(Identifier itemId) {
        return registrations.get(itemId);
    }

    public static WeaponAttributes getAttributes(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        Item item = itemStack.getItem();
        Identifier id = Registry.ITEM.getId(item);
        WeaponAttributes attributes = WeaponRegistry.getAttributes(id);
        return attributes;
    }

    // LOADING

    public static void loadAttributes(ResourceManager resourceManager) {
        loadContainers(resourceManager);

        // Resolving parents
        containers.forEach( (itemId, container) -> {
            if (!Registry.ITEM.containsId(itemId)) {
                return;
            }
            resolveAndRegisterAttributes(itemId, container);
        });
    }

    private static void loadContainers(ResourceManager resourceManager) {
        Gson gson = new Gson();
        Type fileFormat = new TypeToken<AttributesContainer>() {}.getType();
        Map<Identifier, AttributesContainer> containers = new HashMap();
        // Reading all attribute files
        for (Identifier identifier : resourceManager.findResources("weapon_attributes", fileName -> new File(fileName).getPath().endsWith(".json"))) {
            try {
                Resource resource = resourceManager.getResource(identifier);
                // System.out.println("Checking resource: " + identifier);
                JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
                AttributesContainer container = gson.fromJson(reader, fileFormat);
                String id = identifier
                        .toString().replace("weapon_attributes/", "");
                id = id.substring(0, id.lastIndexOf('.'));
                containers.put(new Identifier(id), container);
            } catch (Exception e) {
                System.err.println("Failed to parse: " + identifier);
                e.printStackTrace();
            }
        }
        WeaponRegistry.containers = containers;
    }

    public static void resolveAndRegisterAttributes(Identifier itemId, AttributesContainer container) {
        try {
            ArrayList<WeaponAttributes> resolutionChain = new ArrayList();
            AttributesContainer current = container;
            while (current != null) {
                resolutionChain.add(0, current.attributes());
                if (current.parent() != null) {
                    current = containers.get(new Identifier(current.parent()));
                } else {
                    current = null;
                }
            }

            WeaponAttributes empty = new WeaponAttributes(0, null, false, null,null);
            WeaponAttributes resolvedAttributes = resolutionChain
                .stream()
                .reduce(empty, (a, b) -> {
                    if (b == null) { // I'm not sure why null can enter as `b`
                        return a;
                    }
                    return WeaponAttributesHelper.override(a, b);
                });

            WeaponAttributesHelper.validate(resolvedAttributes);
            register(itemId, resolvedAttributes);
        } catch (Exception e) {
            LOGGER.error("Failed to resolve weapon attributes for: " + itemId + ". Reason: " + e.getMessage());
        }
    }

    // NETWORK SYNC

    private static PacketByteBuf encodedRegistrations = PacketByteBufs.create();

    public static void encodeRegistry() {
        PacketByteBuf buffer = PacketByteBufs.create();
        Gson gson = new Gson();
        String json = gson.toJson(registrations);
        if (BetterCombat.config.weapon_registry_logging) {
            LOGGER.info("Weapon Attribute registry loaded: " + json);
        }

        List<String> chunks = new ArrayList<>();
        int chunkSize = 10000;
        for (int i = 0; i < json.length(); i += chunkSize) {
            chunks.add(json.substring(i, Math.min(json.length(), i + chunkSize)));
        }

        buffer.writeInt(chunks.size());
        for (String chunk: chunks) {
            buffer.writeString(chunk);
        }

        LOGGER.info("Encoded Weapon Attribute registry size (with package overhead): " + buffer.readableBytes()
                + " bytes (in " + chunks.size() + " string chunks with the size of "  + chunkSize + ")");
        encodedRegistrations = buffer;
    }

    public static void decodeRegistry(PacketByteBuf buffer) {
        int chunkCount = buffer.readInt();
        String json = "";
        for (int i = 0; i < chunkCount; ++i) {
            json = json.concat(buffer.readString());
        }
        LOGGER.info("Decoded Weapon Attribute registry in " + chunkCount + " string chunks");
        if (BetterCombat.config.weapon_registry_logging) {
            LOGGER.info("Weapon Attribute registry received: " + json);
        }
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, WeaponAttributes>>() {}.getType();
        Map<String, WeaponAttributes> readRegistrations = gson.fromJson(json, mapType);
        Map<Identifier, WeaponAttributes> newRegistrations = new HashMap();
        readRegistrations.forEach((key, value) -> {
            newRegistrations.put(new Identifier(key), value);
        });
        registrations = newRegistrations;
    }

    public static PacketByteBuf getEncodedRegistry() {
        return encodedRegistrations;
    }
}
