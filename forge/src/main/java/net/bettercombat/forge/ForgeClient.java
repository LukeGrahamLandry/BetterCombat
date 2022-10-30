package net.bettercombat.forge;

import me.shedaniel.autoconfig.AutoConfig;
import net.bettercombat.BetterCombat;
import net.bettercombat.config.ClientConfigWrapper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BetterCombat.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, modScreen) -> AutoConfig.getConfigScreen(ClientConfigWrapper.class, modScreen).get());
    }
}