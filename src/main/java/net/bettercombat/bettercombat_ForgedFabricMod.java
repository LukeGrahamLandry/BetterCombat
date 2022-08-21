package net.bettercombat;

import net.bettercombat.client.BetterCombatClient;
import net.minecraft.sound.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod("bettercombat")
public class bettercombat_ForgedFabricMod {
    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "bettercombat");

    public bettercombat_ForgedFabricMod(){
        new BetterCombat().onInitialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> new BetterCombatClient().onInitializeClient());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
