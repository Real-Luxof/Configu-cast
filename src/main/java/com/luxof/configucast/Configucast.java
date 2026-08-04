package com.luxof.configucast;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent.ExtractMedia;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent.PostExecution;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.PatternIota;

// ((?<!\d)-)?\d+(\.\d+)?|[+\-*\/^%(),]|([A-Za-z]+)(?=\(.+\))|\$[^ +\-*\/^(),\[\]]+|\[(?=[^\]]+\])|\]|\s+
public class Configucast implements ModInitializer {
	public static final String MOD_ID = "configucast";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
			new ConfigucastDatapackLoader()
		);
	}

	public static Identifier id(String name) { return new Identifier(MOD_ID, name); }
}