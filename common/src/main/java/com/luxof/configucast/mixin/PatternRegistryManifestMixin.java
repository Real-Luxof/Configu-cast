package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import com.luxof.configucast.PatternRedirectsStorer;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatternRegistryManifest.class, remap = false)
public class PatternRegistryManifestMixin {
    @Inject(
        method = "matchPattern(Lat/petrak/hexcasting/api/casting/math/HexPattern;Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;Z)Lat/petrak/hexcasting/api/casting/PatternShapeMatch;",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void matchPattern(
        HexPattern pattern,
        CastingEnvironment env,
        boolean cFASO,
        CallbackInfoReturnable<PatternShapeMatch> cir
    ) {
        PatternShapeMatch psm = cir.getReturnValue();

        if (
            !PatternRedirectsStorer.isInEffectForCursedMixins ||
            !PatternRedirectsStorer.hasRedirect(pattern.anglesSignature()) ||
            getFrom(psm) == null
        ) {
            Identifier actionId = idOf(psm);
            if (actionId != null && PatternRedirectsStorer.hasRedirect(actionId))
                cir.setReturnValue(new PatternShapeMatch.Nothing());
            return;
        }

        Identifier redirectedAction = PatternRedirectsStorer.get(pattern.anglesSignature());
        Registry<ActionRegistryEntry> reg = IXplatAbstractions.INSTANCE.getActionRegistry();
        RegistryKey<ActionRegistryEntry> regKey = reg.getKey(reg.get(redirectedAction)).get();

        cir.setReturnValue(
            ScrungledPatternsSave.open(env.getWorld()).lookupReverse(regKey) != null
                ? new PatternShapeMatch.PerWorld(regKey, true)
                : new PatternShapeMatch.Normal(regKey)
        );

    }

    @Nullable private static Identifier idOf(PatternShapeMatch psm) {
        if (psm instanceof PatternShapeMatch.Normal norm) return norm.key.getValue();
        else if (psm instanceof PatternShapeMatch.PerWorld pw) return pw.key.getValue();
        else if (psm instanceof PatternShapeMatch.Special s) return s.key.getValue();
        else return null;
    }

    @Nullable private static RegistryKey<ActionRegistryEntry> getFrom(PatternShapeMatch psm) {
        if (psm instanceof PatternShapeMatch.Normal norm) return norm.key;
        else if (psm instanceof PatternShapeMatch.PerWorld pw) return pw.key;
        else return null;
    }
}
