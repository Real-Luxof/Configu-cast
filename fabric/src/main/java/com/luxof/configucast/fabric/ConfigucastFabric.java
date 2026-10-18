package com.luxof.configucast.fabric;

import com.luxof.configucast.Configucast;
import com.luxof.configucast.ConfigucastDatapackLoader;

import static com.luxof.configucast.Configucast.LOGGER;
import static com.luxof.configucast.Configucast.id;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public final class ConfigucastFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Configucast.init();
        LOGGER.info("Broadcasting to you from a Fabric near you.");
        
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                public static final Identifier ID = id("fabric_datapack_loader");
                @Override public Identifier getFabricId() { return ID; }

                @Override public void reload(ResourceManager manager) {
                    ConfigucastDatapackLoader.reload(manager);
                }
            }
        );
    }
}
