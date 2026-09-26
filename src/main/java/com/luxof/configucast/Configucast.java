package com.luxof.configucast;

import com.luxof.configucast.advancements.ConfigucastAdvancementTriggers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configucast implements ModInitializer {
	public static final String MOD_ID = "configucast";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		ConfigucastAdvancementTriggers.register();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
			new ConfigucastDatapackLoader()
		);
	}

	public static Identifier id(String name) { return new Identifier(MOD_ID, name); }
}