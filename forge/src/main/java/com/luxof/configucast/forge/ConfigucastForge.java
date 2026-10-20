package com.luxof.configucast.forge;

import com.luxof.configucast.Configucast;
import com.luxof.configucast.ConfigucastDatapackLoader;

import static com.luxof.configucast.Configucast.LOGGER;

import dev.architectury.platform.forge.EventBuses;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Configucast.MOD_ID)
@EventBusSubscriber
public final class ConfigucastForge {
    public ConfigucastForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Configucast.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Configucast.init();
        LOGGER.info("--Broadcasting from a Forge near you.");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SynchronousResourceReloader() {
            @Override
            public void reload(ResourceManager manager) {
                ConfigucastDatapackLoader.reload(manager);
            }
        });
    }
}
