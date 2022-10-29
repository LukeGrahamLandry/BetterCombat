package net.bettercombat.forge;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class PlatformClientImpl {
    public static void registerKeyBindings(List<KeyBinding> keyBindings) {
        for(KeyBinding keybinding: keyBindings) {
            KeyBindingHelper.registerKeyBinding(keybinding);
        }
    }
    public static void onEmptyLeftClick(PlayerEntity player) {
        MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickEmpty(player));
    }
}
