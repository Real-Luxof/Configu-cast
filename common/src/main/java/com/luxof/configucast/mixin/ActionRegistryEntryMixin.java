package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.luxof.configucast.PatternRedirectsStorer;

import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ActionRegistryEntry.class, remap = false)
public class ActionRegistryEntryMixin {
    @ModifyReturnValue(
        method = "prototype",
        at = @At("RETURN")
    )
    private HexPattern configucast$lollmaoeven(HexPattern og) {
        if (!PatternRedirectsStorer.isInEffectForCursedMixins) return og;
        Identifier actionId = IXplatAbstractions.INSTANCE.getActionRegistry().getId(
            (ActionRegistryEntry)(Object)this
        );
        return PatternRedirectsStorer.hasRedirect(actionId)
            ? PatternRedirectsStorer.get(actionId) : og;
    }
}
