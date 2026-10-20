package com.luxof.configucast.forge;

import static com.luxof.configucast.Configucast.LOGGER;

import com.luxof.configucast.Configucast;

import dev.architectury.platform.forge.EventBuses;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Configucast.MOD_ID)
public final class ConfigucastForge {
    public ConfigucastForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Configucast.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Configucast.init();
        LOGGER.info("--Broadcasting from a Forge near you.");
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ConfigucastForgeDatapackLoader());
    }
}
