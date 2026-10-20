package com.luxof.configucast;

import com.luxof.configucast.advancements.ConfigucastAdvancementTriggers;

import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configucast {
	public static final String MOD_ID = "configucast";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {

		LOGGER.info("She configure my casts till I multi-platform");

		ConfigucastAdvancementTriggers.register();
		LifecycleEvent.SERVER_STARTING.register(server -> {
			PatternRedirectsStorer.isInEffectForCursedMixins = true;
		});
		LifecycleEvent.SERVER_STOPPING.register(server -> {
			PatternRedirectsStorer.isInEffectForCursedMixins = false;
		});
	}

	public static Identifier id(String name) { return new Identifier(MOD_ID, name); }
}
