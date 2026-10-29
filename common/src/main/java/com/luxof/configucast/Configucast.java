package com.luxof.configucast;

import com.luxof.configucast.advancements.ConfigucastAdvancementTriggers;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;

import dev.architectury.event.events.common.LifecycleEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configucast {
	public static final String MOD_ID = "configucast";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Ew
	public static MinecraftServer runningServer = null;

	public static void init() {

		LOGGER.info("She configure my casts till I multi-platform");

		ConfigucastAdvancementTriggers.register();
		LifecycleEvent.SERVER_STARTING.register(server -> {
			runningServer = server;
			PatternRedirectsStorer.isInEffectForCursedMixins = true;
		});
		LifecycleEvent.SERVER_STOPPING.register(server -> {
			runningServer = server;
			PatternRedirectsStorer.isInEffectForCursedMixins = false;
		});
	}

	public static Identifier id(String name) { return new Identifier(MOD_ID, name); }
	public static void prepareVariables(
        long originalAmount,
        CastingEnvironment env,
        CastingImage image,
        Map<String, Object> variables
	) {
        variables.putIfAbsent("originalAmount", originalAmount);
        variables.putIfAbsent("casterExists", env.getCastingEntity() != null);
        variables.putIfAbsent("caster", env.getCastingEntity());
        variables.putIfAbsent("mishapSprayPos", env.mishapSprayPos());
        variables.putIfAbsent("enlightened", env.isEnlightened());
        variables.putIfAbsent("totalMedia", (Long.MAX_VALUE - env.extractMedia(Long.MAX_VALUE, true)) / 10000.0);
        variables.putIfAbsent("castingHand", env.getCastingHand().ordinal());
        variables.putIfAbsent("otherHand", env.getOtherHand().ordinal());
        variables.putIfAbsent("parenCount", image.getParenCount());
        variables.putIfAbsent("parenthesized", image.getParenthesized());
        variables.putIfAbsent("userData", image.getUserData());
        variables.putIfAbsent("opsConsumed", image.getOpsConsumed());
		List<Iota> stack = image.getStack();
		for (int i = stack.size() - 1; i >= 0; i--) {
			variables.putIfAbsent("i", stack.get(i));
		}
	}
}
