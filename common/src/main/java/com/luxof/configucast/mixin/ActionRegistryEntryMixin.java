package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import com.luxof.configucast.PatternRedirectsStorer;

import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ActionRegistryEntry.class, remap = false)
public class ActionRegistryEntryMixin {
    @Inject(
        method = "prototype",
        at = @At("RETURN"),
        cancellable = true
    )
    private void configucast$lollmaoeven(CallbackInfoReturnable<HexPattern> cir) {
        if (!PatternRedirectsStorer.isInEffectForCursedMixins) return;
        Identifier actionId = IXplatAbstractions.INSTANCE.getActionRegistry().getId(
            (ActionRegistryEntry)(Object)this
        );
        if (PatternRedirectsStorer.hasRedirect(actionId))
            cir.setReturnValue(PatternRedirectsStorer.get(actionId));
    }
}
